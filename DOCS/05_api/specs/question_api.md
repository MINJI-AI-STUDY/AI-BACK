# Question API 상세 명세

## 1. 문제 생성
- `POST /api/teacher/materials/{materialId}/question-sets/generate`

### Request Body
| key | 타입 | 설명 | 필수 |
| --- | --- | --- | --- |
| questionCount | number | 생성 문항 수 (1~10) | Y |
| difficulty | string | EASY/MEDIUM/HARD | Y |

### 핵심 정책
- 4지선다
- 정답 1개
- explanation 필수
- conceptTags 1~2개 필수

## 2. 문제 세트 조회
- `GET /api/teacher/question-sets/{questionSetId}`

## 3. 문제 수정
- `PATCH /api/teacher/question-sets/{questionSetId}/questions/{questionId}`

## 4. 문제 배포
- `POST /api/teacher/question-sets/{questionSetId}/publish`
- 제외되지 않은 문항이 1개 이상이어야 배포 가능
