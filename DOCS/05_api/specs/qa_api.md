# QA API 상세 명세

## 1. 자료 기반 질의응답
- `POST /api/student/materials/{materialId}/qa`

### Request Body
| key | 타입 | 설명 | 필수 |
| --- | --- | --- | --- |
| question | string | 질문(1~500자) | Y |

### Response
| key | 타입 | 설명 |
| --- | --- | --- |
| answer | string | 답변 |
| evidenceSnippets | string[] | 근거 snippet (최대 2개) |
| grounded | boolean | grounded 여부 |
| insufficientEvidence | boolean | 근거 부족 안내 여부 |

### 정책
- grounded false 또는 AI 실패 시 fallback 응답 가능
- QALog에 status 저장 (`SUCCESS`, `INSUFFICIENT_EVIDENCE`, `FALLBACK`)
