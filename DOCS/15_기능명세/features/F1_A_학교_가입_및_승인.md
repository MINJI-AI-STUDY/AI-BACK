# F1-A 학교 가입 및 승인

## 기능 요약
- 교직원은 학교 선택, 로그인 ID, 비밀번호, 학교 이메일로 가입 요청을 생성합니다.
- 학생은 학교 선택, 실명, (선택) 학급으로 가입 요청을 생성합니다.
- 학교별 운영자는 같은 학교의 가입 요청을 승인/반려합니다.

## API 명세
- `GET /api/signup/schools?keyword=`
- `POST /api/signup/teacher`
- `POST /api/signup/student`
- `GET /api/signup/requests/pending?schoolId=`
- `PATCH /api/signup/requests/{signupRequestId}`

## 정책

### POL-SIGNUP-001 학교 마스터 정책
- 전국 초중고 학교는 내부 `school_master` 테이블에서 조회합니다.

### POL-SIGNUP-002 교직원 가입 정책
- 교직원은 학교 이메일 도메인이 일치하거나 학교 운영자 승인으로 가입합니다.

### POL-SIGNUP-003 학생 가입 정책
- 학생은 실명으로 가입 요청을 생성하고 학교 운영자 승인을 거쳐 활성화됩니다.

### POL-SIGNUP-004 승인 로그 정책
- 승인/반려 이력은 `approval_audit_logs`에 저장합니다.
