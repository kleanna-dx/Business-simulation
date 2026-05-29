-- =====================================================================
-- V0_1__auth_seed_data.sql
-- 인증 시드 데이터 — 권한/역할매핑 + 샘플 사용자 5명
-- ⚠️ 모든 샘플 계정 공통 비밀번호: Precost!2026  (BCrypt, 로컬 전용)
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------- 권한(Permission) ------------------------
INSERT INTO permission (code, description) VALUES
  ('MASTER_READ',        '기준정보 조회'),
  ('MASTER_WRITE',       '기준정보 등록/수정'),
  ('PRODUCTION_READ',    '생산계획 조회'),
  ('PRODUCTION_WRITE',   '생산계획 등록/수정'),
  ('MOVEMENT_READ',      '입출고 조회'),
  ('MOVEMENT_WRITE',     '입출고 등록/수정'),
  ('PRICE_READ',         '단가변동 조회'),
  ('PRICE_WRITE',        '단가변동 등록/수정'),
  ('SIMULATION_READ',    '원가 시뮬레이션 조회'),
  ('SIMULATION_WRITE',   '원가 시뮬레이션 실행'),
  ('APPROVAL_PROCESS',   '전자결재 처리(승인/반려)'),
  ('DASHBOARD_READ',     '대시보드 조회'),
  ('USER_MANAGE',        '사용자 관리'),
  ('SYSTEM_ADMIN',       '시스템 관리');

-- ---------------------------- 역할-권한 매핑 --------------------------
-- ADMIN: 전체 권한
INSERT INTO role_permission (role, permission_id)
  SELECT 'ADMIN', id FROM permission;

-- COST(원가팀): 시뮬레이션 + 조회 전반
INSERT INTO role_permission (role, permission_id)
  SELECT 'COST', id FROM permission
  WHERE code IN ('MASTER_READ','PRODUCTION_READ','MOVEMENT_READ','PRICE_READ',
                 'SIMULATION_READ','SIMULATION_WRITE','DASHBOARD_READ');

-- PRODUCTION(생산팀): 생산계획 등록 + 조회
INSERT INTO role_permission (role, permission_id)
  SELECT 'PRODUCTION', id FROM permission
  WHERE code IN ('MASTER_READ','PRODUCTION_READ','PRODUCTION_WRITE',
                 'MOVEMENT_READ','DASHBOARD_READ');

-- PURCHASE(구매팀): 단가변동 등록 + 조회
INSERT INTO role_permission (role, permission_id)
  SELECT 'PURCHASE', id FROM permission
  WHERE code IN ('MASTER_READ','PRICE_READ','PRICE_WRITE',
                 'MOVEMENT_READ','DASHBOARD_READ');

-- LOGISTICS(물류팀): 입출고 등록 + 조회
INSERT INTO role_permission (role, permission_id)
  SELECT 'LOGISTICS', id FROM permission
  WHERE code IN ('MASTER_READ','MOVEMENT_READ','MOVEMENT_WRITE','DASHBOARD_READ');

-- TEAM_LEAD(팀장): 조회 전반 + 결재
INSERT INTO role_permission (role, permission_id)
  SELECT 'TEAM_LEAD', id FROM permission
  WHERE code IN ('MASTER_READ','PRODUCTION_READ','MOVEMENT_READ','PRICE_READ',
                 'SIMULATION_READ','APPROVAL_PROCESS','DASHBOARD_READ');

-- FACTORY_MANAGER(공장장): 조회 전반 + 결재
INSERT INTO role_permission (role, permission_id)
  SELECT 'FACTORY_MANAGER', id FROM permission
  WHERE code IN ('MASTER_READ','PRODUCTION_READ','MOVEMENT_READ','PRICE_READ',
                 'SIMULATION_READ','APPROVAL_PROCESS','DASHBOARD_READ');

-- USER(일반): 대시보드 조회만
INSERT INTO role_permission (role, permission_id)
  SELECT 'USER', id FROM permission
  WHERE code IN ('DASHBOARD_READ');

-- ---------------------------- 샘플 사용자 5명 -------------------------
-- 공통 비밀번호: Precost!2026 (BCrypt, 사용자별 고유 salt)
INSERT INTO users
  (username, password_hash, full_name, email, department, role, status,
   failed_login_count, password_changed_at, created_at, created_by) VALUES
  ('admin',     '$2b$10$DV53Rlto3hyhlvRC9DpI0uqRZozSVQKEa6VFrXJfBvpxkXageKtfi',
   '시스템관리자', 'admin@company.com',     'IT운영팀', 'ADMIN',          'ACTIVE', 0, NOW(), NOW(), 'system'),
  ('cost1',     '$2b$10$BcPxOato6wOVn5E19EgIEOfBgcTAeP2u9ljt3.KmoQrMJ6H3FkE4m',
   '원가담당',   'cost1@company.com',     '원가팀',   'COST',           'ACTIVE', 0, NOW(), NOW(), 'system'),
  ('prod1',     '$2b$10$G12w5qHsH98rdtyZBSmSlecDtvl3cBY7fAJo5ioJONGbgboDn3wfi',
   '생산담당',   'prod1@company.com',     '생산팀',   'PRODUCTION',     'ACTIVE', 0, NOW(), NOW(), 'system'),
  ('purchase1', '$2b$10$K6C52b3QXfArgh901DI.GeU7hcnTnYJ0va89iwxsn0Ww46rn1r4E.',
   '구매담당',   'purchase1@company.com', '구매팀',   'PURCHASE',       'ACTIVE', 0, NOW(), NOW(), 'system'),
  ('manager1',  '$2b$10$9XkXlaobXtRWXvgXtra/ruvvmnIAL46CMZHTV6MkG44Y1surzrPDe',
   '김공장장',   'manager1@company.com',  '공장경영', 'FACTORY_MANAGER','ACTIVE', 0, NOW(), NOW(), 'system');
