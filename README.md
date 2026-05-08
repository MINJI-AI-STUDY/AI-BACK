# AI-STUDY Backend

AI-STUDY는 수업 자료 업로드, 문제 생성, 학생 풀이/채점, 결과 확인, 자료 기반 질의응답을 연결하는 교육 지원 MVP입니다. 이 저장소는 그중 Spring Boot 기반 백엔드 API와 데이터/운영 문서를 관리합니다.

> 작업 원칙: GitHub Flow와 `1기능 = 1브랜치 = 1PR` 기준으로 변경 이력을 남깁니다.

## 현재 백엔드 범위

- 인증과 역할 기반 진입 흐름
- 학교/학급 단위 논리 스코프와 가입 승인 흐름
- 교사용 자료 업로드, 분석 재시도, 자료 조회
- 문제 세트 생성, 편집, 게시
- 학생 문제 풀이, 제출, 결과 확인
- 교사/운영자 대시보드 조회
- 자료 범위 기반 AI 질의응답 연동 경계
- 채널 메시지, 접속 상태, SSE 기반 실시간 흐름

## 기술 스택

- Java 21
- Spring Boot 3.5.0
- Spring Web / Spring Data JPA / Spring Security
- Flyway
- H2 local profile, PostgreSQL runtime configuration
- Gradle Wrapper

## 핵심 코드 바로가기

| 영역 | 주요 파일 |
|---|---|
| 애플리케이션 진입점 | [`ApiApplication.java`](src/main/java/com/aistudy/api/ApiApplication.java) |
| 인증 API | [`AuthController.java`](src/main/java/com/aistudy/api/auth/AuthController.java) |
| 학교 가입/조회 | [`SignupController.java`](src/main/java/com/aistudy/api/signup/controller/SignupController.java) |
| 운영자 학교/사용자 관리 | [`AdminDirectoryController.java`](src/main/java/com/aistudy/api/admin/AdminDirectoryController.java) |
| 교사용 자료 API | [`MaterialController.java`](src/main/java/com/aistudy/api/material/controller/MaterialController.java) |
| 문제 생성/게시 API | [`TeacherQuestionController.java`](src/main/java/com/aistudy/api/question/controller/TeacherQuestionController.java) |
| 학생 제출/결과 API | [`StudentSubmissionController.java`](src/main/java/com/aistudy/api/submission/controller/StudentSubmissionController.java) |
| 대시보드 API | [`DashboardController.java`](src/main/java/com/aistudy/api/dashboard/controller/DashboardController.java) |
| AI 질의응답 API | [`QaController.java`](src/main/java/com/aistudy/api/qa/controller/QaController.java) |
| 실시간 채널 API | [`ChannelRealtimeController.java`](src/main/java/com/aistudy/api/channel/controller/ChannelRealtimeController.java) |
| AI 서버 연동 경계 | [`AiIntegrationService.java`](src/main/java/com/aistudy/api/common/integration/AiIntegrationService.java) |
| DB 마이그레이션 | [`src/main/resources/db/migration`](src/main/resources/db/migration) |

## 문서 바로가기

- [프로젝트 범위](DOCS/01_개요/프로젝트_범위.md)
- [저장소 구조](DOCS/01_개요/저장소_구조.md)
- [백엔드 아키텍처](DOCS/02_아키텍처/백엔드_아키텍처.md)
- [API 카탈로그](DOCS/05_API명세/API_카탈로그.md)
- [상세 API 명세](DOCS/05_API명세/상세명세/README.md)
- [기능 명세](DOCS/15_기능명세/README.md)
- [백엔드 구현 준비 문서](DOCS/15_기능명세/backend/README.md)
- [릴리즈 게이트](DOCS/10_운영/BACKEND_RELEASE_GATE.md)

## 로컬 실행

```bash
./gradlew bootRun --args="--spring.profiles.active=local"
```

Windows 환경에서는 아래 명령을 사용할 수 있습니다.

```powershell
./gradlew.bat bootRun --no-daemon
```

기본 설정은 [`application.properties`](src/main/resources/application.properties)에 있고, 로컬 실행용 H2/Flyway 설정은 [`application-local.properties`](src/main/resources/application-local.properties)에 분리되어 있습니다.

## 검증

```bash
./gradlew test
./gradlew build
./gradlew assemble
```

대표 테스트 파일:

- [`ApiApplicationTests.java`](src/test/java/com/aistudy/api/ApiApplicationTests.java)
- [`AiIntegrationServiceTest.java`](src/test/java/com/aistudy/api/common/integration/AiIntegrationServiceTest.java)
- [`SchoolMasterSyncServiceIntegrationTest.java`](src/test/java/com/aistudy/api/signup/service/SchoolMasterSyncServiceIntegrationTest.java)

## 운영/배포 관련 파일

- [`railway.json`](railway.json): Railway 실행 명령
- [`Dockerfile`](Dockerfile): 컨테이너 빌드 기준
- [`DOCS/10_운영`](DOCS/10_운영): 릴리즈, 롤백, 수동 QA 문서

## 현재 한계

- 루트 README는 검증 동선 제공을 위해 정리한 문서이며, 상세 API 설명은 `DOCS/05_API명세`를 기준으로 확인합니다.
- OpenAPI/Swagger 설정은 확인되지 않았으므로 markdown API 문서를 우선 기준으로 둡니다.
- 운영 환경의 민감값은 환경변수로 주입해야 하며, 실제 secret 값은 저장소에 포함하지 않습니다.
- 일부 backend hub 문서가 참조하는 보조 문서가 아직 저장소에 없을 수 있습니다.
