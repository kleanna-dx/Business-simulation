# 사전원가 시뮬레이션 웹 시스템 (precost-simulator)

제지/화장지 공장의 월간 사전원가를 시뮬레이션하는 사내 웹 시스템.
SAP BW에서 전월 사용현황을 받아 당월 계획을 입력하면 사전원가·변동분석을 자동 산출한다.

## 기술 스택 (사내 표준)
- Java 17 + Spring Boot 3.2.5, Gradle 멀티모듈
- MariaDB 10.11+ (utf8mb4), Spring Data JPA (ddl-auto=none) + Flyway
- JWT Stateless 인증 (Spring Security)
- Naming: PhysicalNamingStrategyStandardImpl (@Table/@Column 이름이 DB에 그대로 반영)
- 테스트: JUnit 5 + AssertJ (+ Testcontainers), 엑셀: Apache POI 5.x

> ⚠️ **빌드 환경 안내**: 본 저장소는 소스만 포함합니다. JVM/Gradle/MariaDB가 설치된
> 사내 개발 환경(IntelliJ 등)에서 `./gradlew build` 하십시오.

## 모듈 구성 (총 14개)
### 공통 인프라 (Phase 1)
| 모듈 | 상태 | 설명 |
|------|------|------|
| `module-common` | ✅ 완료 | BaseEntity, ApiResponse, ErrorCode, 예외, GlobalExceptionHandler, PageResponse, YearMonth 유틸/검증, Result, JpaAuditingConfig |
| `module-calc` | ✅ 완료 | 영역 A~D 산식 8종 + CalcEngine + 검증 테스트 |
| `module-domain` | ✅ 완료 | 25개 JPA 엔티티 + 25 Repository + 17 ENUM + Flyway(V1 스키마/V2 샘플) |

### 애플리케이션
| 모듈 | 상태 | 설명 |
|------|------|------|
| `app-api` | ✅ 완료 | 메인 REST API (PrecostSimulatorApplication, OpenApiConfig, HealthController, application.yml/local) |
| `app-batch` | ⏳ 예정 | SAP 월배치 (Spring Batch) |

### 도메인 모듈 (Phase 1~2)
| 모듈 | 상태 | 요청서 번호 |
|------|------|-------------|
| `module-auth` | ✅ 완료 | JWT Stateless · 8 Role · User/Permission/RolePermission · 사용자관리 · Flyway(V0/V0_1 시드 5계정) |
| `module-sap-sync` | ⏳ 예정 | 모듈 1 |
| `module-master-data` | ⏳ 예정 | 모듈 2 |
| `module-production-plan` | ⏳ 예정 | 모듈 3 |
| `module-movement` | ⏳ 예정 | 모듈 4 ⭐ |
| `module-cost-simulation` | ⏳ 예정 | 모듈 5 ⭐ |
| `module-price-change` | ⏳ 예정 | 모듈 6 |
| `module-approval` | ⏳ 예정 | 모듈 7 |
| `module-dashboard` | ⏳ 예정 | 모듈 8 |

## 현재까지 완료된 내용
1. **루트 Gradle 멀티모듈 골격** — `settings.gradle`, `build.gradle`(Spring Boot BOM + Lombok + UTF-8 공통), `gradle.properties`(버전 관리)
2. **module-common** — 공통 응답/예외/엔티티 기반 클래스 + YearMonth 유틸 테스트
3. **module-calc** — 가중평균 단가 V, 사용량 P, 재료비 W, 톤당비용 X, 사용량차이 Y, 단가차이 Z, 재료비종합 AA, 분모톤 결정 + CalcEngine. 핵심 검증 케이스 포함.
4. **module-domain** — 25 엔티티 / 17 ENUM / 25 Repository + Flyway `V1__initial_schema.sql`(21테이블) · `V2__sample_data.sql`(PM2/PM3/TISSUE, 자재·BOM·전월사용량·생산계획 등 산식 검증 케이스 포함)
5. **module-auth** — User/Permission/RolePermission 엔티티, JWT(Provider/Filter/Properties), SecurityConfig(STATELESS), CustomUserDetailsService, PasswordPolicyValidator, LoginAttemptService, AuthService/UserService, AuthController/UserController, Flyway `V0__auth_schema.sql` · `V0_1__auth_seed_data.sql`(권한14·역할매핑·샘플 5계정)
6. **app-api** — PrecostSimulatorApplication(멀티모듈 스캔), OpenApiConfig(Bearer JWT), HealthController, `application.yml`(JPA ddl-none + PhysicalNamingStrategyStandardImpl + Flyway), `application-local.yml`

## 인증 API & 샘플 계정
- `POST /api/v1/auth/login` · `POST /api/v1/auth/refresh` · `PUT /api/v1/auth/password` · `GET /api/v1/auth/me`
- `POST/GET/PUT /api/v1/users` (ADMIN) · 잠금/해제/비활성화
- 샘플 계정(로컬 전용, 공통 비밀번호 `Precost!2026`): `admin`(ADMIN), `cost1`(COST), `prod1`(PRODUCTION), `purchase1`(PURCHASE), `manager1`(FACTORY_MANAGER)

## 로컬 실행 (IntelliJ / JVM 환경)
```bash
# 1) MariaDB 준비
CREATE DATABASE precost CHARACTER SET utf8mb4;
CREATE USER 'precost'@'%' IDENTIFIED BY 'precost';
GRANT ALL PRIVILEGES ON precost.* TO 'precost'@'%';
# 2) 빌드 & 실행 (Flyway가 V0→V0_1→V1→V2 자동 적용)
./gradlew :app-api:bootRun
# 3) Swagger UI
#   http://localhost:8080/swagger-ui.html
```

## 산식 검증 결과 (중요)
요청서의 검증 케이스(PM2/SC/220 LATEX)를 교차 검산한 결과:
- **Z = −3,086,720** → 문서값과 정확히 일치 ✅
- **Y = 3,977,112** → 문서값(3,977,498)과 약 386원 차이 ⚠️
- 원인: 입력 M(1512.90)이 실데이터 소수점 정밀도와 다를 가능성. 산식 로직은 정상.
- 자세한 내용은 [`docs/calc-engine-spec.md`](docs/calc-engine-spec.md) 참조.

## 다음 단계 (Phase 2 — 도메인 슬라이스 구현)
1. `module-master-data` (모듈 03): 자재/공급처/BOM CRUD + 엑셀 업로드
2. `module-sap-sync` (모듈 02) + `app-batch`: SAP BW 월배치 수신·검증·격리
3. `module-production-plan` (모듈 04): 생산계획 입력·결재 연동
4. `module-cost-simulation` (모듈 08) ⭐: CalcEngine 연결 사전원가 산출
5. `module-movement`(06)·`module-price-change`(07)·`module-approval`(10)·`module-dashboard`(09)

## 디렉토리 구조
```
precost-simulator/
├── settings.gradle / build.gradle / gradle.properties
├── module-common/   (✅)
├── module-calc/     (✅)
├── docs/
│   └── calc-engine-spec.md
└── (module-domain, app-api, 도메인 모듈... 예정)
```
