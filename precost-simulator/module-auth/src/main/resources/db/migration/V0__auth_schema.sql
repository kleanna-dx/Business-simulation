-- =====================================================================
-- V0__auth_schema.sql
-- 인증/인가 스키마 (모듈 01) — 도메인 스키마(V1)보다 먼저 적용
-- MariaDB 10.11+ / utf8mb4
-- =====================================================================

SET NAMES utf8mb4;

CREATE TABLE users (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    username            VARCHAR(50)  NOT NULL,
    password_hash       VARCHAR(100) NOT NULL,
    full_name           VARCHAR(50)  NOT NULL,
    email               VARCHAR(100) NULL,
    department          VARCHAR(50)  NULL,
    role                VARCHAR(20)  NOT NULL,   -- RoleType (STRING)
    status              VARCHAR(10)  NOT NULL,   -- UserStatus (STRING)
    failed_login_count  INT          NOT NULL DEFAULT 0,
    last_login_at       DATETIME     NULL,
    password_changed_at DATETIME     NULL,
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NULL,
    created_by          VARCHAR(50)  NULL,
    updated_by          VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_USERS_USERNAME (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자';

CREATE TABLE permission (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(50)  NOT NULL,
    description VARCHAR(200) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_PERMISSION_CODE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='권한(기능 단위)';

CREATE TABLE role_permission (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    role          VARCHAR(20) NOT NULL,   -- RoleType (STRING)
    permission_id BIGINT      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_ROLE_PERMISSION (role, permission_id),
    KEY idx_rp_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='역할-권한 매핑';
