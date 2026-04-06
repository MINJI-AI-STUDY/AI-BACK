# Backend API 초안

## 공통 원칙
- 모든 API는 `/api` 하위에 둡니다.
- 인증이 필요한 요청은 Bearer JWT를 사용합니다.
- 역할은 `TEACHER`, `STUDENT`, `OPERATOR` 세 가지로 고정합니다.
- 에러 응답은 `code`, `message`, `retryable` 필드를 가집니다.

## F1 인증/권한

### POST `/api/auth/login`
- 요청: `loginId`, `password`
- 응답: `accessToken`, `role`, `displayName`
- 비고: 데모 계정은 SQLite seed 데이터로 제공합니다.

### GET `/api/auth/me`
- 응답: `userId`, `role`, `displayName`

## F2 자료 업로드/분석

### POST `/api/teacher/materials`
- 요청: multipart `file`, `title`, `description`
- 응답: `materialId`, `status=UPLOADED`

### GET `/api/teacher/materials/{materialId}`
- 응답: `materialId`, `title`, `description`, `status`, `failureReason?`

### POST `/api/teacher/materials/{materialId}/retry`
- 요청: 없음
- 응답: `materialId`, `status=PROCESSING`
- 비고: 기존 FAILED 자료만 재처리 가능

## F3 문제 생성/검토/배포

### POST `/api/teacher/materials/{materialId}/question-sets/generate`
- 요청: `questionCount`, `difficulty(EASY|MEDIUM|HARD)`
- 응답: `questionSetId`, `status=REVIEW_REQUIRED`, `questions[]`

### PATCH `/api/teacher/question-sets/{questionSetId}/questions/{questionId}`
- 요청: `stem`, `options[4]`, `correctOptionIndex`, `explanation`, `conceptTags[1..2]`, `excluded`
- 응답: 수정된 question

### POST `/api/teacher/question-sets/{questionSetId}/publish`
- 요청: `dueAt?`
- 응답: `questionSetId`, `status=PUBLISHED`, `distributionCode`, `distributionLink`

## F4 학생 풀이/자동 채점

### GET `/api/student/question-sets/{distributionCode}`
- 응답: `questionSetId`, `title`, `dueAt?`, `questions[]`

### POST `/api/student/question-sets/{distributionCode}/submissions`
- 요청: `answers[{questionId, selectedOptionIndex}]`
- 응답: `submissionId`, `score`, `questionResults[]`

## F5 결과/대시보드

### GET `/api/student/submissions/{submissionId}/result`
- 응답: `score`, `questionResults[]`, `explanations[]`

### GET `/api/teacher/question-sets/{questionSetId}/dashboard`
- 응답: `studentScores[]`, `questionAccuracy[]`, `weakConceptTags[]`

### GET `/api/operator/overview`
- 응답: `averageScore`, `participationRate`, `completionRate`

## F6 자료 기반 AI 질의응답

### POST `/api/student/materials/{materialId}/qa`
- 요청: `question`
- 응답: `answer`, `evidenceSnippets[]`, `grounded`, `insufficientEvidence`