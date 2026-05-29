-- =====================================================================
-- V1__initial_schema.sql
-- 사전원가 시뮬레이션 시스템 - 도메인(업무) 스키마
-- MariaDB 10.11+ / utf8mb4
-- 명명규칙: PhysicalNamingStrategyStandardImpl (@Table/@Column 명을 그대로 사용)
-- 금액 = BIGINT(원), 수량 = DECIMAL(18,4), 단가/원단위소비 = DECIMAL(18,6)
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 공통 감사 컬럼 규약 (모든 BaseEntity 상속 테이블)
--   id BIGINT AUTO_INCREMENT PK
--   created_at DATETIME NOT NULL
--   updated_at DATETIME NULL
--   created_by VARCHAR(50) NULL
--   updated_by VARCHAR(50) NULL
-- ---------------------------------------------------------------------

-- =========================== 모듈 03 기준정보 ==========================

CREATE TABLE plant (
    plant_code  VARCHAR(10)  NOT NULL,
    plant_name  VARCHAR(100) NOT NULL,
    PRIMARY KEY (plant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='공장(PM2/PM3/TISSUE)';

CREATE TABLE chemical_group (
    group_code  VARCHAR(50)  NOT NULL,
    group_name  VARCHAR(100) NOT NULL,
    PRIMARY KEY (group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='약품 그룹';

CREATE TABLE material (
    material_code  VARCHAR(7)   NOT NULL,
    material_name  VARCHAR(200) NOT NULL,
    category       VARCHAR(20)  NOT NULL,    -- MaterialCategory (STRING)
    chemical_group VARCHAR(50)  NULL,
    unit           VARCHAR(10)  NOT NULL,
    status         VARCHAR(10)  NOT NULL,    -- ActiveStatus (STRING)
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NULL,
    created_by     VARCHAR(50)  NULL,
    updated_by     VARCHAR(50)  NULL,
    PRIMARY KEY (material_code),
    KEY idx_material_category (category),
    KEY idx_material_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='자재 마스터';

CREATE TABLE supplier (
    supplier_code  VARCHAR(20)  NOT NULL,
    supplier_name  VARCHAR(200) NOT NULL,
    biz_no         VARCHAR(20)  NULL,
    contact_name   VARCHAR(50)  NULL,
    contact_phone  VARCHAR(30)  NULL,
    status         VARCHAR(10)  NOT NULL,    -- ActiveStatus (STRING)
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NULL,
    created_by     VARCHAR(50)  NULL,
    updated_by     VARCHAR(50)  NULL,
    PRIMARY KEY (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='공급처 마스터';

CREATE TABLE product_grade (
    grade_code      VARCHAR(20)  NOT NULL,
    grade_name      VARCHAR(100) NOT NULL,
    parent_category VARCHAR(50)  NULL,
    PRIMARY KEY (grade_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='제품 등급(지종)';

CREATE TABLE bom (
    id                        BIGINT       NOT NULL AUTO_INCREMENT,
    material_code             VARCHAR(7)   NOT NULL,
    plant_code                VARCHAR(10)  NOT NULL,   -- Plant (STRING)
    grade_code                VARCHAR(20)  NOT NULL,
    standard_unit_consumption DECIMAL(18,6) NOT NULL,
    denominator_type          VARCHAR(20)  NOT NULL,   -- DenominatorType (STRING)
    created_at                DATETIME     NOT NULL,
    updated_at                DATETIME     NULL,
    created_by                VARCHAR(50)  NULL,
    updated_by                VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_BOM_MATERIAL_PLANT_GRADE (material_code, plant_code, grade_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM(표준원단위)';

CREATE TABLE movement_type (
    type_code   VARCHAR(20)  NOT NULL,   -- MovementType (STRING)
    description VARCHAR(100) NOT NULL,
    is_inbound  TINYINT(1)   NOT NULL,
    PRIMARY KEY (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='입출고 유형';

-- =========================== 모듈 02 SAP/BW 연동 ========================

CREATE TABLE sap_sync_job (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    sync_type     VARCHAR(20)  NOT NULL,    -- SyncType (STRING)
    status        VARCHAR(10)  NOT NULL,    -- SyncStatus (STRING)
    started_at    DATETIME     NULL,
    finished_at   DATETIME     NULL,
    record_count  INT          NULL,
    error_message VARCHAR(2000) NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NULL,
    created_by    VARCHAR(50)  NULL,
    updated_by    VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY idx_sync_started (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SAP/BW 동기화 작업 이력';

CREATE TABLE bw_raw_snapshot (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    sync_job_id   BIGINT       NOT NULL,
    raw_data      JSON         NOT NULL,
    snapshot_date DATE         NOT NULL,
    processed     TINYINT(1)   NOT NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NULL,
    created_by    VARCHAR(50)  NULL,
    updated_by    VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY idx_bw_processed (processed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BW 원천 스냅샷';

CREATE TABLE quarantine_row (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    sync_job_id   BIGINT       NOT NULL,
    raw_row       JSON         NOT NULL,
    error_reason  VARCHAR(1000) NOT NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NULL,
    created_by    VARCHAR(50)  NULL,
    updated_by    VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY idx_quarantine_job (sync_job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='검증실패 격리 행';

CREATE TABLE previous_month_usage (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    plant_code           VARCHAR(10)   NOT NULL,   -- Plant (STRING)
    target_month         VARCHAR(7)    NOT NULL,   -- 'YYYY-MM'
    material_code        VARCHAR(7)    NOT NULL,
    grade_code           VARCHAR(20)   NOT NULL,
    basis_weight         DECIMAL(10,2) NOT NULL,
    prev_usage_kg        DECIMAL(18,4) NULL,        -- K
    prev_unit_consumption DECIMAL(18,6) NULL,       -- L (전월 원단위)
    prev_unit_price      DECIMAL(18,4) NULL,        -- M (전월 단가)
    prev_material_cost   BIGINT        NULL,         -- N (재료비, 원)
    prev_total_production DECIMAL(18,4) NULL,        -- 전월 생산량
    prev_defect_rate     DECIMAL(5,4)  NULL,
    bw_volume_variance   BIGINT        NULL,         -- BW 물량차이(검증용)
    bw_price_variance    BIGINT        NULL,         -- BW 단가차이(검증용)
    created_at           DATETIME      NOT NULL,
    updated_at           DATETIME      NULL,
    created_by           VARCHAR(50)   NULL,
    updated_by           VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_PMU_PLANT_MONTH_MATERIAL_GRADE_BW
        (plant_code, target_month, material_code, grade_code, basis_weight),
    KEY idx_pmu_plant_month (plant_code, target_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='전월 사용량(L/M/N 등 산식 입력)';

-- =========================== 모듈 04 생산계획 ==========================

CREATE TABLE production_plan (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    plant_code            VARCHAR(10)   NOT NULL,   -- Plant (STRING)
    target_month          VARCHAR(7)    NOT NULL,
    production_volume_ton DECIMAL(18,4) NOT NULL,   -- X3
    defect_rate           DECIMAL(5,4)  NOT NULL,   -- X7
    grade_breakdown       JSON          NULL,
    status                VARCHAR(20)   NOT NULL,   -- PlanStatus (STRING)
    approval_id           BIGINT        NULL,
    submitted_at          DATETIME      NULL,
    approved_at           DATETIME      NULL,
    created_at            DATETIME      NOT NULL,
    updated_at            DATETIME      NULL,
    created_by            VARCHAR(50)   NULL,
    updated_by            VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_PP_PLANT_MONTH (plant_code, target_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='생산계획(X3 생산량/X7 불량률)';

CREATE TABLE production_actual (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    production_plan_id BIGINT        NOT NULL,
    actual_date        DATE          NOT NULL,
    actual_volume_ton  DECIMAL(18,4) NOT NULL,
    actual_defect_qty  DECIMAL(18,4) NULL,
    created_at         DATETIME      NOT NULL,
    updated_at         DATETIME      NULL,
    created_by         VARCHAR(50)   NULL,
    updated_by         VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    KEY idx_pa_plan (production_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='생산실적';

-- =========================== 모듈 06 입출고/물류 =======================

CREATE TABLE movement (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    movement_type      VARCHAR(20)   NOT NULL,   -- MovementType (STRING)
    material_code      VARCHAR(7)    NOT NULL,
    from_location_type VARCHAR(20)   NULL,        -- LocationType (STRING)
    from_location_code VARCHAR(50)   NULL,
    to_location_type   VARCHAR(20)   NULL,        -- LocationType (STRING)
    to_location_code   VARCHAR(50)   NULL,
    plan_qty_ton       DECIMAL(18,4) NOT NULL,
    actual_qty_ton     DECIMAL(18,4) NULL,
    unit_price         DECIMAL(18,4) NULL,
    plan_date          DATE          NOT NULL,
    actual_date        DATE          NULL,
    reference_no       VARCHAR(50)   NULL,
    status             VARCHAR(20)   NOT NULL,   -- MovementStatus (STRING)
    approval_id        BIGINT        NULL,
    remark             VARCHAR(500)  NULL,
    created_at         DATETIME      NOT NULL,
    updated_at         DATETIME      NULL,
    created_by         VARCHAR(50)   NULL,
    updated_by         VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    KEY idx_movement_material (material_code),
    KEY idx_movement_type_status (movement_type, status),
    KEY idx_movement_plan_date (plan_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='입출고/물류 이동';

CREATE TABLE opening_stock (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    plant_code         VARCHAR(10)   NOT NULL,   -- Plant (STRING)
    target_month       VARCHAR(7)    NOT NULL,
    material_code      VARCHAR(7)    NOT NULL,
    opening_qty_ton    DECIMAL(18,4) NOT NULL,   -- T
    opening_unit_price DECIMAL(18,4) NOT NULL,   -- U
    created_at         DATETIME      NOT NULL,
    updated_at         DATETIME      NULL,
    created_by         VARCHAR(50)   NULL,
    updated_by         VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_OS_PLANT_MONTH_MATERIAL (plant_code, target_month, material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='기초재고(T 수량/U 단가)';

-- =========================== 모듈 07 단가변동 ==========================

CREATE TABLE price_change (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    material_code  VARCHAR(7)    NOT NULL,
    supplier_code  VARCHAR(20)   NULL,
    previous_price DECIMAL(18,4) NOT NULL,
    new_price      DECIMAL(18,4) NOT NULL,
    increase_rate  DECIMAL(7,4)  NOT NULL,    -- 인상률(5% 초과 시 전결)
    burden_rate    DECIMAL(5,4)  NOT NULL,
    effective_date DATE          NOT NULL,
    status         VARCHAR(20)   NOT NULL,    -- PriceChangeStatus (STRING)
    approval_id    BIGINT        NULL,
    remark         VARCHAR(500)  NULL,
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NULL,
    created_by     VARCHAR(50)   NULL,
    updated_by     VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    KEY idx_pc_material (material_code),
    KEY idx_pc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='단가변동';

CREATE TABLE price_history (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    material_code  VARCHAR(7)    NOT NULL,
    price          DECIMAL(18,4) NOT NULL,
    effective_date DATE          NOT NULL,
    source         VARCHAR(20)   NOT NULL,    -- PriceSource (STRING)
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NULL,
    created_by     VARCHAR(50)   NULL,
    updated_by     VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    KEY idx_ph_material_date (material_code, effective_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='단가 이력';

-- =========================== 모듈 08 원가 시뮬레이션 ===================

CREATE TABLE cost_scenario (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    plant_code    VARCHAR(10)  NOT NULL,    -- Plant (STRING)
    target_month  VARCHAR(7)   NOT NULL,
    scenario_name VARCHAR(100) NOT NULL,
    description   VARCHAR(500) NULL,
    status        VARCHAR(20)  NOT NULL,    -- ScenarioStatus (STRING)
    base_inputs   JSON         NULL,
    approval_id   BIGINT       NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NULL,
    created_by    VARCHAR(50)  NULL,
    updated_by    VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY idx_cs_plant_month (plant_code, target_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='원가 시뮬레이션 시나리오';

CREATE TABLE cost_simulation_result (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    scenario_id       BIGINT        NOT NULL,
    material_code     VARCHAR(7)    NOT NULL,
    L_prev_unit       DECIMAL(18,6) NULL,    -- L 전월 원단위
    M_prev_price      DECIMAL(18,4) NULL,    -- M 전월 단가
    Q_curr_unit       DECIMAL(18,6) NULL,    -- Q 당월 원단위
    V_curr_price      DECIMAL(18,4) NULL,    -- V 가중평균단가
    P_usage_kg        DECIMAL(18,4) NULL,    -- P 월사용량
    W_cost_million    DECIMAL(18,4) NULL,    -- W 재료비(백만)
    X_cost_per_ton    DECIMAL(18,4) NULL,    -- X 톤당원가
    Y_volume_variance BIGINT        NULL,    -- Y 물량차이(원)
    Z_price_variance  BIGINT        NULL,    -- Z 단가차이(원)
    AA_total          BIGINT        NULL,    -- AA 총차이(원)
    created_at        DATETIME      NOT NULL,
    updated_at        DATETIME      NULL,
    created_by        VARCHAR(50)   NULL,
    updated_by        VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    KEY idx_csr_scenario (scenario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='시뮬레이션 자재별 결과(L~AA)';

CREATE TABLE variance_report (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    scenario_id           BIGINT        NOT NULL,
    total_cost_per_ton    DECIMAL(18,4) NULL,
    total_material_cost   BIGINT        NULL,
    volume_variance_total BIGINT        NULL,
    price_variance_total  BIGINT        NULL,
    top5_materials        JSON          NULL,
    created_at            DATETIME      NOT NULL,
    updated_at            DATETIME      NULL,
    created_by            VARCHAR(50)   NULL,
    updated_by            VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    KEY idx_vr_scenario (scenario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='차이분석 리포트';

-- =========================== 모듈 10 전자결재 ==========================

CREATE TABLE approval (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    target_type  VARCHAR(30)  NOT NULL,    -- ApprovalTargetType (STRING)
    target_id    BIGINT       NOT NULL,
    title        VARCHAR(200) NOT NULL,
    drafter_id   BIGINT       NOT NULL,
    current_step INT          NOT NULL,
    status       VARCHAR(20)  NOT NULL,    -- ApprovalStatus (STRING)
    submitted_at DATETIME     NULL,
    finished_at  DATETIME     NULL,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NULL,
    created_by   VARCHAR(50)  NULL,
    updated_by   VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY idx_approval_target (target_type, target_id),
    KEY idx_approval_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='전자결재 문서';

CREATE TABLE approval_step (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    approval_id     BIGINT       NOT NULL,
    step_order      INT          NOT NULL,
    approver_id     BIGINT       NOT NULL,
    role            VARCHAR(50)  NULL,
    action          VARCHAR(20)  NOT NULL,    -- ApprovalStepAction (STRING)
    comment         TEXT         NULL,
    processed_at    DATETIME     NULL,
    delegated_to_id BIGINT       NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NULL,
    created_by      VARCHAR(50)  NULL,
    updated_by      VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY idx_step_approval (approval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='결재 단계(라인)';

CREATE TABLE approval_agreer (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    approval_id BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NULL,
    created_by  VARCHAR(50) NULL,
    updated_by  VARCHAR(50) NULL,
    PRIMARY KEY (id),
    KEY idx_agreer_approval (approval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='결재 합의자';

-- =========================== 공통 인프라 ==============================

CREATE TABLE audit_log (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    target_type VARCHAR(50) NOT NULL,
    target_id   VARCHAR(50) NOT NULL,
    action      VARCHAR(20) NOT NULL,
    before_data JSON        NULL,
    after_data  JSON        NULL,
    changed_by  VARCHAR(50) NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NULL,
    created_by  VARCHAR(50) NULL,
    updated_by  VARCHAR(50) NULL,
    PRIMARY KEY (id),
    KEY idx_audit_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='감사 로그';

CREATE TABLE dashboard_alert (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    alert_type     VARCHAR(30)  NOT NULL,
    severity       VARCHAR(10)  NOT NULL,
    title          VARCHAR(200) NOT NULL,
    message        VARCHAR(1000) NULL,
    target_url     VARCHAR(300) NULL,
    target_user_id BIGINT       NULL,
    is_read        TINYINT(1)   NOT NULL,
    expires_at     DATETIME     NULL,
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NULL,
    created_by     VARCHAR(50)  NULL,
    updated_by     VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY idx_alert_user_read (target_user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='대시보드 알림';
