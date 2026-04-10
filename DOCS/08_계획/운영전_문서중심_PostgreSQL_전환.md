# 운영 전 문서 중심 PostgreSQL 전환 계획

## 목표
- 인메모리 MVP 백엔드를 PostgreSQL 기반으로 전환한다.
- 학교 단위 멀티테넌시는 유지하고, 업로드 문서(PDF) 단위 식별/조회/대시보드 축을 추가한다.
- 교사는 학교 내 문서별 통계/생성 문제/학생 질문을 조회하고, 학생은 문서별 자신의 질문 이력을 조회할 수 있게 만든다.

## 핵심 결정

### 1. 멀티테넌시 경계
- 최상위 tenant 경계는 기존과 동일하게 `schoolId`를 사용한다.
- 교사는 같은 `schoolId` 범위의 문서를 조회할 수 있다.
- 학생은 같은 `schoolId` 범위 문서만 접근 가능하며 QA 로그는 자신의 데이터만 조회한다.

### 2. 문서 식별자 전략
- 내부 기준 식별자는 전역 고유 `documentId`(현 코드의 `materialId`)를 사용한다.
- 운영/화면 표시용 번호로 `docNo`를 추가한다.
- `docNo`는 학교별 증가 번호이며 `(school_id, doc_no)` 유니크로 관리한다.

### 3. 조회 축
- 기존 문제세트 대시보드는 유지한다.
- 문서(PDF) 단위 대시보드를 추가한다.
- 문서 대시보드에는 최소 아래를 포함한다.
  - 문서 요약 정보
  - 생성된 문제세트 목록
  - 제출 수 / 참여 학생 수 / 평균 점수
  - QA 로그 수 / 최근 학생 질문

## 데이터 모델 변경

### materials
- `id`
- `school_id`
- `teacher_id`
- `doc_no`
- `title`
- `description`
- `original_file_name`
- `file_path`
- `status`
- `failure_reason`
- `extracted_text`
- `created_at`
- `updated_at`

### question_sets
- `id`
- `school_id`
- `material_id`
- `teacher_id`
- `difficulty`
- `status`
- `distribution_code`
- `distribution_link`
- `due_at`
- `created_at`

### questions
- `id`
- `question_set_id`
- `stem`
- `correct_option_index`
- `explanation`
- `excluded`
- 옵션/개념 태그는 별도 컬렉션 테이블로 관리

### submissions
- `id`
- `school_id`
- `material_id`
- `question_set_id`
- `student_id`
- `score`
- `submitted_at`

### submission_answer_results
- `id`
- `submission_id`
- `question_id`
- `selected_option_index`
- `correct`
- `explanation`
- concept tag는 별도 컬렉션 테이블로 관리

### qa_logs
- `id`
- `school_id`
- `material_id`
- `student_id`
- `question`
- `answer`
- `grounded`
- `status`
- `created_at`
- evidence snippet은 별도 컬렉션 테이블로 관리

## API 변경

### 교사
- 기존 유지: `POST /api/teacher/materials`
- 기존 유지: `GET /api/teacher/materials/{materialId}`
- 추가: `GET /api/teacher/materials/{materialId}/dashboard`
- 추가: `GET /api/teacher/materials/{materialId}/qa-logs`
- 추가: `GET /api/teacher/materials/{materialId}/question-sets`

### 학생
- 기존 유지: `POST /api/student/materials/{materialId}/qa`
- 추가: `GET /api/student/materials/{materialId}/qa-logs/me`

## 구현 순서
1. PostgreSQL + Flyway 도입
2. materials / question_sets / submissions / qa_logs 영속화 전환
3. schoolId/docNo 기반 접근 제어 추가
4. 문서 대시보드/QA 조회 API 추가
5. 프론트에서 문서 대시보드/학생 질문 이력 노출

## 운영 고려사항
- Flyway를 단일 스키마 변경 진입점으로 사용한다.
- `ddl-auto`는 `validate`로 두고 운영 스키마 변경은 migration으로만 수행한다.
- PDF 원본은 파일 스토리지에 두고 DB에는 메타데이터/경로만 저장한다.
- 문서별/학교별 조회가 많으므로 `materials(school_id, doc_no)`, `question_sets(material_id)`, `submissions(material_id, student_id)`, `qa_logs(material_id, student_id, created_at)` 인덱스를 유지한다.
