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
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
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
  "schoolId": "school-a",
  "classroomId": "class-a",
  "role": "TEACHER",
  "displayName": "교사 데모"
}
```

## 3. 토큰 재발급
- `POST /api/auth/refresh`

### Request Body
```json
{
  "refreshToken": "refresh-token"
}
```

### Response
```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "new-refresh-token",
  "role": "TEACHER",
  "displayName": "교사 데모"
}
```

## 4. 로그아웃
- `POST /api/auth/logout`

### Request Body
```json
{
  "refreshToken": "refresh-token"
}
```
