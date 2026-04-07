# Auth API 상세 명세

## 1. 로그인

### 메서드 / URL
- `POST /api/auth/login`

### Request Header
| key | value |
| --- | --- |
| Content-Type | application/json |

### Request Body
| key | 타입 | 설명 | 필수 |
| --- | --- | --- | --- |
| loginId | string | 로그인 ID | Y |
| password | string | 비밀번호 | Y |

```json
{
  "loginId": "teacher",
  "password": "teacher123"
}
```

### Response
```json
{
  "accessToken": "dGVhY2hlcg==",
  "role": "TEACHER",
  "displayName": "교사 데모"
}
```

### Error Codes
| code | HTTP | 설명 |
| --- | --- | --- |
| AUTH_UNAUTHORIZED | 401 | 로그인 정보 불일치 |

## 2. 현재 사용자 조회

### 메서드 / URL
- `GET /api/auth/me`

### Request Header
| key | value |
| --- | --- |
| Authorization | Bearer {accessToken} |

### Response
```json
{
  "userId": "teacher-1",
  "role": "TEACHER",
  "displayName": "교사 데모"
}
```
