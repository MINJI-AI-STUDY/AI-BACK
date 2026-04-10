# MVP 구현 보고서

## 목적
- current 문서 기준으로 MVP 구현 진행 상황, 추후 수정 포인트, 사용자 인지 필요사항을 기록합니다.

## 현재 진행
- 로컬 스캐폴드 생성 완료: frontend(5173), api(8080), ai(8000)
- 백엔드 선행 구현 완료: F1~F6 API가 로컬 8080에서 응답 확인됨
- 프론트 1:1 API 매핑 구현 완료
- AI 서버 MVP 엔드포인트 확장 완료: `/health`, `/extract-material`, `/generate-questions`, `/qa`

## 구현 순서 근거
- 본 세션에서 백엔드 F1~F6 API와 AI 8000 포트 검증을 먼저 완료한 뒤 프론트 5173 구현과 브라우저 QA를 진행했습니다.
- 백엔드 검증 이후 프론트는 해당 API 계약을 기준으로 타입/화면/체크리스트를 맞췄습니다.

## 검증 근거
- 프론트 build 성공: `frontend npm run build`
- 백엔드 test 성공: `api ./gradlew.bat test`
- 백엔드 수동 QA 성공: 로그인 → 자료 업로드 → 문제 생성 → 배포 → 제출 → 결과 → 대시보드 → 운영자 요약 응답 확인
- AI 수동 QA 성공: `8000` 포트에서 `/health`, `/extract-material`, `/generate-questions`, `/qa` 응답 확인
- API+AI 통합 QA 성공: `8080`의 F2/F3/F6 경로가 `8000`과 연결된 상태에서 응답 확인
- 프론트 브라우저 QA 성공: 학생 데모 로그인 → 배포 코드 참여 → 제출 → 결과 페이지 진입 확인
- 프론트 브라우저 QA 성공: 교사 데모 로그인 → 자료 업로드 → 문제 생성 → 검토 → 배포 완료 확인
- 프론트 브라우저 QA 성공: 운영자 데모 로그인 → `/operator` 실데이터 화면 확인
- 프론트 검토 화면 보강: 문제 검토 페이지가 백엔드 단건 조회와 수정 저장 UI(stem/options/explanation/conceptTags/excluded)를 사용하도록 정렬
- F3 직접 조회 검증 성공: 깨끗한 API 프로세스에서 생성 직후 `GET /api/teacher/question-sets/{questionSetId}`가 `REVIEW_REQUIRED`로 정상 응답 확인

## 추후 수정 필요
- AI 서버의 실제 PDF 분석/문제생성 고도화
- 보안 토큰 전략 강화
- SQLite 영속 계층 정교화 여부 재검토
- Windows PowerShell 출력에서 한글 응답 인코딩이 깨져 보이는 현상 확인 필요
- FastAPI 연동 실패 시 현재는 자료 기반 fallback 답변으로 닫히므로, 추후 실제 LLM/RAG 성공 경로 검증을 추가해야 함

## 사용자 인지 필요사항
- 현재 구현은 학생 중심 경량 UI 우선 순서로 진행합니다.
- 교사/운영자 고도화는 MVP 범위 안에서 필수 기능 위주로 제한합니다.

## 기술 고민 / ADR 연계
- ADR-001: 백엔드 도메인 구조를 domain-first로 잠금
- ADR-002: AI 연동 경계를 Spring/FastAPI 분리로 잠금
- ADR-003: 프론트 상태 전략을 화면 단위 fetch 중심으로 잠금
