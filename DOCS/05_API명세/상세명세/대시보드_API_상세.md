# Dashboard API 상세 명세

## 1. 교사 대시보드
- `GET /api/teacher/question-sets/{questionSetId}/dashboard`

### Response
- `studentScores[]`
- `questionAccuracy[]`
- `weakConceptTags[]`

## 2. 운영자 overview
- `GET /api/operator/overview`

### Response
- `averageScore`
- `participationRate`
- `completionRate`

### 정책
- 운영자는 학생 상세 없이 요약 지표만 조회
