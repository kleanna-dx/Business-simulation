-- =====================================================================
-- V2__sample_data.sql
-- 개발/로컬 검증용 샘플 데이터
-- 산식 검증 케이스(PM2 / SC / 220 / LATEX) 포함
-- =====================================================================

SET NAMES utf8mb4;

-- =========================== 공장 ====================================
INSERT INTO plant (plant_code, plant_name) VALUES
  ('PM2',    'PM2 초지기'),
  ('PM3',    'PM3 초지기'),
  ('TISSUE', '위생용지(Tissue)');

-- =========================== 약품 그룹 ================================
INSERT INTO chemical_group (group_code, group_name) VALUES
  ('SIZING',    '사이징제'),
  ('RETENTION', '보류제'),
  ('STRENGTH',  '지력증강제'),
  ('DEFOAMER',  '소포제'),
  ('DYE',       '염료/착색제'),
  ('BIOCIDE',   '슬라임 컨트롤');

-- =========================== 제품 등급(지종) =========================
INSERT INTO product_grade (grade_code, grade_name, parent_category) VALUES
  ('SC',     'SC지(상질 코팅원지)', '인쇄용지'),
  ('IV',     'IV지(중질지)',        '인쇄용지'),
  ('CCKB',   'CCKB(백판지)',        '판지'),
  ('TISSUE', '화장지 원단',          '위생용지');

-- =========================== 자재 마스터 =============================
-- material_code 7자리 규칙: 11xxx 펄프, 12xxx 폐지, 13xxx 약품 ...
INSERT INTO material (material_code, material_name, category, chemical_group, unit, status, created_at, created_by) VALUES
  ('1100005', 'UKP 활엽수펄프',   'PULP',      NULL,        'KG', 'ACTIVE', NOW(), 'system'),
  ('1100023', 'UKP 침엽수펄프',   'PULP',      NULL,        'KG', 'ACTIVE', NOW(), 'system'),
  ('1100100', 'LATEX 라텍스',     'PULP',      NULL,        'KG', 'ACTIVE', NOW(), 'system'),
  ('1100200', 'BKP 표백크라프트', 'PULP',      NULL,        'KG', 'ACTIVE', NOW(), 'system'),
  ('1200010', '신문 폐지',        'WASTE_PAPER', NULL,      'KG', 'ACTIVE', NOW(), 'system'),
  ('1200020', '잡지 폐지',        'WASTE_PAPER', NULL,      'KG', 'ACTIVE', NOW(), 'system'),
  ('1300010', 'AKD 사이징제',     'CHEMICAL',  'SIZING',    'KG', 'ACTIVE', NOW(), 'system'),
  ('1300020', '양이온보류제',     'CHEMICAL',  'RETENTION', 'KG', 'ACTIVE', NOW(), 'system'),
  ('1300030', '전분 지력증강제',  'CHEMICAL',  'STRENGTH',  'KG', 'ACTIVE', NOW(), 'system'),
  ('1300040', '소포제',           'CHEMICAL',  'DEFOAMER',  'KG', 'ACTIVE', NOW(), 'system'),
  ('1400010', '청색 염료',        'INK',       'DYE',       'KG', 'ACTIVE', NOW(), 'system'),
  ('1500010', '코어 지관',        'SUB_MATERIAL', NULL,     'EA', 'ACTIVE', NOW(), 'system');

-- =========================== 공급처 마스터 ===========================
INSERT INTO supplier (supplier_code, supplier_name, biz_no, contact_name, contact_phone, status, created_at, created_by) VALUES
  ('SUP-PULP01', '한솔펄프상사',   '123-45-67890', '김펄프', '02-1111-2222', 'ACTIVE', NOW(), 'system'),
  ('SUP-CHEM01', '대한약품',       '234-56-78901', '이약품', '031-333-4444', 'ACTIVE', NOW(), 'system'),
  ('SUP-WP01',   '재활용자원',     '345-67-89012', '박폐지', '032-555-6666', 'ACTIVE', NOW(), 'system');

-- =========================== 입출고 유형 =============================
INSERT INTO movement_type (type_code, description, is_inbound) VALUES
  ('INBOUND',    '입고',     1),
  ('OUTBOUND',   '출고',     0),
  ('TRANSFER',   '재고이동', 0),
  ('ADJUSTMENT', '재고조정', 0),
  ('SHIPMENT',   '출하',     0),
  ('RETURN',     '반품',     1);

-- =========================== BOM(표준원단위) =========================
INSERT INTO bom (material_code, plant_code, grade_code, standard_unit_consumption, denominator_type, created_at, created_by) VALUES
  ('1100100', 'PM2', 'SC',     5.364300, 'SC',    NOW(), 'system'),  -- 검증케이스 LATEX
  ('1100005', 'PM2', 'SC',   850.000000, 'KB',    NOW(), 'system'),  -- UKP -> KB 분모
  ('1100023', 'PM2', 'SC',   320.000000, 'KB',    NOW(), 'system'),
  ('1300010', 'PM2', 'SC',     2.500000, 'SC',    NOW(), 'system'),
  ('1300020', 'PM2', 'SC',     1.200000, 'SC',    NOW(), 'system'),
  ('1200010', 'PM3', 'IV',   620.000000, 'IV',    NOW(), 'system'),
  ('1200020', 'PM3', 'IV',   210.000000, 'IV',    NOW(), 'system'),
  ('1400010', 'TISSUE', 'TISSUE', 0.350000, 'TOTAL', NOW(), 'system');

-- =========================== 기초재고(T/U) ===========================
-- 가중평균단가 V 검증: 기초재고 T=2.0ton, U=500원 등
INSERT INTO opening_stock (plant_code, target_month, material_code, opening_qty_ton, opening_unit_price, created_at, created_by) VALUES
  ('PM2', '2026-04', '1100100', 2.0000,  500.0000, NOW(), 'system'),
  ('PM2', '2026-04', '1100005', 50.0000, 1200.0000, NOW(), 'system'),
  ('PM3', '2026-04', '1200010', 80.0000, 180.0000,  NOW(), 'system');

-- =========================== 생산계획(X3/X7) =========================
-- 검증케이스: PM2 / 2026-04 / 생산량(X3)=16,000 ton
INSERT INTO production_plan (plant_code, target_month, production_volume_ton, defect_rate, grade_breakdown, status, created_at, created_by) VALUES
  ('PM2', '2026-04', 16000.0000, 0.0150, '{"SC":16000}', 'APPROVED', NOW(), 'system'),
  ('PM3', '2026-04', 12000.0000, 0.0200, '{"IV":12000}', 'DRAFT',    NOW(), 'system'),
  ('TISSUE', '2026-04', 4500.0000, 0.0100, '{"TISSUE":4500}', 'DRAFT', NOW(), 'system');

-- =========================== 전월 사용량(L/M/N) ======================
-- 산식 검증 핵심 행: PM2 / SC / 220(평량) / LATEX(1100100)
--   L = 5.3643 (전월 원단위)
--   M = 1512.90 (전월 단가)
--   당월 입력: Q = 5.20, V = 1550 → CalcEngine 으로 Y/Z/AA 산출
--   ※ TODO[실데이터 확정]: 문서 기댓값(Y=3,977,498)과 386원 차이.
--     M의 소수 정밀도 차이로 추정, usage_2604.xlsx 수령 후 확정 예정.
INSERT INTO previous_month_usage
  (plant_code, target_month, material_code, grade_code, basis_weight,
   prev_usage_kg, prev_unit_consumption, prev_unit_price, prev_material_cost,
   prev_total_production, prev_defect_rate, bw_volume_variance, bw_price_variance,
   created_at, created_by) VALUES
  ('PM2', '2026-03', '1100100', 'SC', 220.00,
   85828.8000, 5.364300, 1512.9000, 129848000,
   16000.0000, 0.0150, 3977498, -3086720,
   NOW(), 'system'),
  ('PM2', '2026-03', '1100005', 'SC', 220.00,
   13600000.0000, 850.000000, 1200.0000, 16320000000,
   16000.0000, 0.0150, NULL, NULL,
   NOW(), 'system'),
  ('PM3', '2026-03', '1200010', 'IV', 80.00,
   7440000.0000, 620.000000, 180.0000, 1339200000,
   12000.0000, 0.0200, NULL, NULL,
   NOW(), 'system');

-- =========================== 단가 이력 ===============================
INSERT INTO price_history (material_code, price, effective_date, source, created_at, created_by) VALUES
  ('1100100', 1512.9000, '2026-03-01', 'INITIAL', NOW(), 'system'),
  ('1100100', 1550.0000, '2026-04-01', 'PRICE_CHANGE', NOW(), 'system'),
  ('1100005', 1200.0000, '2026-03-01', 'INITIAL', NOW(), 'system'),
  ('1200010', 180.0000,  '2026-03-01', 'INITIAL', NOW(), 'system');

-- =========================== 단가변동 ================================
-- LATEX 1512.90 -> 1550.00 (인상률 약 2.45%, 5% 이하 일반결재)
INSERT INTO price_change
  (material_code, supplier_code, previous_price, new_price, increase_rate, burden_rate,
   effective_date, status, remark, created_at, created_by) VALUES
  ('1100100', 'SUP-PULP01', 1512.9000, 1550.0000, 0.0245, 1.0000,
   '2026-04-01', 'APPLIED', 'LATEX 단가 인상(분기 조정)', NOW(), 'system'),
  ('1100005', 'SUP-PULP01', 1200.0000, 1290.0000, 0.0750, 1.0000,
   '2026-04-01', 'SUBMITTED', 'UKP 5% 초과 인상 → 전결 라우팅', NOW(), 'system');

-- =========================== 원가 시뮬레이션 시나리오 ================
INSERT INTO cost_scenario
  (plant_code, target_month, scenario_name, description, status, base_inputs, created_at, created_by) VALUES
  ('PM2', '2026-04', '2026년 4월 PM2 기준안',
   'LATEX 단가 인상 반영 사전원가', 'DRAFT',
   '{"productionVolumeTon":16000,"defectRate":0.015}', NOW(), 'system');

-- =========================== 대시보드 알림 ===========================
INSERT INTO dashboard_alert
  (alert_type, severity, title, message, target_url, target_user_id, is_read, created_at, created_by) VALUES
  ('PRICE_CHANGE', 'HIGH', 'UKP 단가 5% 초과 인상',
   '자재 1100005 인상률 7.5% → 전결 승인 필요', '/price-changes', NULL, 0, NOW(), 'system'),
  ('SYNC', 'INFO', 'SAP/BW 동기화 완료',
   '2026-04 전월사용량 동기화 정상 완료', '/sap-sync', NULL, 0, NOW(), 'system');
