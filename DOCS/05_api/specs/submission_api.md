# Submission API 상세 명세

## 1. 학생 문제 세트 조회
- `GET /api/student/question-sets/{distributionCode}`

### Response 핵심 필드
- `questionSetId`
- `materialId`
- `title`
- `dueAt`
- `questions[]`

## 2. 학생 제출
- `POST /api/student/question-sets/{distributionCode}/submissions`

### Request Body
| key | 타입 | 설명 | 필수 |
| --- | --- | --- | --- |
| answers | array | 답안 목록 | Y |

### 정책
- 문제 세트별 1회 제출
- 모든 문항 응답 필수
- 마감 이후 제출 불가

## 3. 학생 결과 조회
- `GET /api/student/submissions/{submissionId}/result`
- 제출 후에만 접근 가능
