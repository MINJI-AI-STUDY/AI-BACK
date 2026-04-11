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
| channelId | string | 연결할 채널 ID | N(미지정 시 기본 채널) |
| title | string | 자료 제목 | Y |
| description | string | 자료 설명 | N |

### Response
```json
{
  "materialId": "material-uuid",
  "docNo": 1,
  "schoolId": "school-a",
  "channelId": "school-a-channel-1",
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

## 3. 교사 자료 목록 조회
- `GET /api/teacher/materials`
- 같은 학교 교사 자료를 최신순으로 조회합니다.

## 4. 학생 자료 목록 조회
- `GET /api/student/materials`
- 같은 학교에서 `READY` 상태인 자료를 학생 홈에 자동 노출합니다.

## 5. 자료 재처리
- `POST /api/teacher/materials/{materialId}/retry`
- `FAILED` 상태 자료만 가능

## 6. 자료 문서 조회
- `GET /api/materials/document/{materialId}`
- 학생/교사/운영자 모두 권한 범위 내에서 PDF inline 응답을 받을 수 있습니다.
- 기존 teacher 전용 경로는 호환성 유지 목적이 아니라면 점진 제거 대상으로 관리합니다.
