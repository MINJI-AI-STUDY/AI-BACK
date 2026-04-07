# Material API 상세 명세

## 1. 자료 업로드

### 메서드 / URL
- `POST /api/teacher/materials`

### Request Header
| key | value |
| --- | --- |
| Authorization | Bearer {accessToken} |
| Content-Type | multipart/form-data |

### Form Data
| key | 타입 | 설명 | 필수 |
| --- | --- | --- | --- |
| file | file(pdf) | 업로드 PDF | Y |
| title | string | 자료 제목 | Y |
| description | string | 자료 설명 | N |

### Response
```json
{
  "materialId": "material-uuid",
  "title": "AIDA1 교재",
  "description": "설명",
  "status": "READY",
  "failureReason": null
}
```

### Error Codes
| code | HTTP | 설명 |
| --- | --- | --- |
| BAD_REQUEST | 400 | 빈 파일 / 비PDF / 20MB 초과 |
| FORBIDDEN | 403 | 교사 권한 없음 |

## 2. 자료 상태 조회
- `GET /api/teacher/materials/{materialId}`

## 3. 자료 재처리
- `POST /api/teacher/materials/{materialId}/retry`
- `FAILED` 상태 자료만 가능

## 4. 자료 문서 조회
- `GET /api/teacher/materials/document/{materialId}`
- PDF inline 응답
