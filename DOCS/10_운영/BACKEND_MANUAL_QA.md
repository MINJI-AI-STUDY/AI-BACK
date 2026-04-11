# BACKEND_MANUAL_QA

## 1. 목적
이 문서는 AI-BACK의 핵심 API와 권한 흐름을 수동 검증하기 위한 기준을 정의한다.

## 관련 문서
- 공통 수동 QA: `../../../DOCS/10_운영/MANUAL_QA_MATRIX.md`
- 백엔드 게이트: `BACKEND_RELEASE_GATE.md`
- 백엔드 rollback: `BACKEND_ROLLBACK.md`
- 증거 템플릿: `../../../DOCS/10_운영/EVIDENCE_TEMPLATE.md`

## 2. 우선 시나리오
- 로그인 성공/실패
- teacher / student / operator 권한 구분
- 자료 업로드 및 조회
- 문제 생성/배포
- 제출/채점 결과 조회
- QA 요청 및 로그 조회

## 3. 시나리오 템플릿
- 시나리오명:
- 사전 조건:
- 요청 경로:
- 입력:
- 기대 결과:
- 실제 결과:
- 판정:
- 증거:

## 4. 최소 배포 차단 시나리오
- 역할별 로그인 실패
- 권한 없는 API가 열려 있음
- 자료/문제/제출/대시보드/QA 중 핵심 API 실패
- 운영자 범위 초과 조회 허용

## 5. 실제 QA 입력
- base API URL:
- teacher 토큰/계정:
- student 토큰/계정:
- operator 토큰/계정:
- 테스트 일시:
- 수행자:
