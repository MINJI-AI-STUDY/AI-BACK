# API 카탈로그

| 도메인 | 메서드 | URL | 설명 |
| --- | --- | --- | --- |
| auth | POST | `/api/auth/login` | 로그인 |
| auth | GET | `/api/auth/me` | 현재 사용자 조회 |
| material | POST | `/api/teacher/materials` | 자료 업로드 |
| material | GET | `/api/teacher/materials/{materialId}` | 자료 상태 조회 |
| material | POST | `/api/teacher/materials/{materialId}/retry` | 자료 재처리 |
| material | GET | `/api/teacher/materials/document/{materialId}` | 자료 PDF inline 조회 |
| question | POST | `/api/teacher/materials/{materialId}/question-sets/generate` | 문제 생성 |
| question | GET | `/api/teacher/question-sets/{questionSetId}` | 문제 세트 조회 |
| question | PATCH | `/api/teacher/question-sets/{questionSetId}/questions/{questionId}` | 문항 수정 |
| question | POST | `/api/teacher/question-sets/{questionSetId}/publish` | 문제 배포 |
| submission | GET | `/api/student/question-sets/{distributionCode}` | 학생 문제 조회 |
| submission | POST | `/api/student/question-sets/{distributionCode}/submissions` | 학생 제출 |
| submission | GET | `/api/student/submissions/{submissionId}/result` | 학생 결과 조회 |
| dashboard | GET | `/api/teacher/question-sets/{questionSetId}/dashboard` | 교사 대시보드 |
| dashboard | GET | `/api/operator/overview` | 운영자 overview |
| qa | POST | `/api/student/materials/{materialId}/qa` | 자료 기반 질의응답 |
