# BACKEND_RELEASE_GATE

## 1. 목적
이 문서는 AI-BACK 저장소가 배포직전 완료로 판정되기 위한 백엔드 전용 게이트를 정의한다.

## 2. 상위 기준 문서
- 루트 `DOCS/10_운영/RELEASE_GATE.md`
- 루트 `DOCS/00_기준/ONE_SHOT_DELIVERY_CONTRACT.md`
- `AI-BACK/DOCS/00_기준/통합_개발_테스트_방법론.md`
- `AI-BACK/DOCS/10_운영/BACKEND_MANUAL_QA.md`
- `AI-BACK/DOCS/10_운영/BACKEND_ROLLBACK.md`
- 루트 `DOCS/10_운영/EVIDENCE_TEMPLATE.md`
- 루트 `DOCS/07_정책/ENV_SOURCE_OF_TRUTH.md`

## 3. 필수 명령
- 테스트: `./gradlew test`
- 빌드: `./gradlew build`
- 로컬 구동 확인: `./gradlew bootRun --args="--spring.profiles.active=local"`

명령명이 문서와 실제 빌드 스크립트 간에 달라지면 본 문서를 먼저 갱신한다.

## 4. 필수 게이트

### 4.1 테스트 게이트
- 변경 범위 관련 테스트가 통과해야 한다.
- 테스트 추가가 없으면 이유와 대체 검증을 남긴다.

### 4.2 빌드 게이트
- `./gradlew build`가 성공해야 한다.
- 빌드 실패 상태에서는 배포직전 완료로 보지 않는다.

### 4.3 인증/권한 게이트
- teacher, student, operator 권한 경계가 유지되어야 한다.
- 비인가 API 접근 차단이 확인되어야 한다.
- 로그인 및 토큰 기반 접근 흐름이 깨지지 않아야 한다.

### 4.4 API smoke 게이트
아래 핵심 범위가 정상 동작해야 한다.
- auth
- material
- question
- submission
- dashboard
- qa

### 4.5 마이그레이션 게이트
- schema 변경 여부를 기록해야 한다.
- Flyway 적용 위험을 검토해야 한다.
- rollback 불가 migration이면 forward-fix 기준을 같이 남긴다.

## 5. 배포 플랫폼 기준
- 운영 배포 플랫폼 기준은 Railway다.
- 시작 명령 기준은 `AI-BACK/railway.json`의 `java -jar /app/app.jar`를 따른다.
- Railway 환경값과 루트 env 문서가 일치해야 한다.

## 6. 필수 확인 항목
- [ ] 테스트 통과
- [ ] 빌드 성공
- [ ] 인증/권한 흐름 검증
- [ ] 주요 API smoke 확인
- [ ] DB migration 위험 검토 완료
- [ ] env/secret 요구사항 식별 완료

## 7. 배포 차단 조건
- 로그인 실패
- 권한 차단 실패
- 핵심 API 5xx 지속 발생
- migration 영향 미확인
- 운영 env/secret 미확정

## 8. 증거 기록
완료 보고에는 아래를 포함한다.
- 실행 명령
- 테스트/빌드 결과
- 핵심 API smoke 결과
- migration 검토 내용
- known issue

## 9. 실제 운영 입력 항목
- Railway backend URL:
- DB URL 입력 위치:
- JWT secret 책임자:
- APP_AI_BASE_URL 실제 값:
- CORS 허용 origin:
- migration 승인자:
