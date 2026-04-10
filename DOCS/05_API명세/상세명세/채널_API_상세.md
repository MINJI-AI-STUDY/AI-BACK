# Channel API 상세 명세

## 교사 채널 목록
- `GET /api/teacher/channels`

## 학생 채널 목록
- `GET /api/student/channels`

## 교사 채널 생성
- `POST /api/teacher/channels`

## 교사 채널 수정
- `PATCH /api/teacher/channels/{channelId}`

## 교사 채널 워크스페이스
- `GET /api/teacher/channels/{channelId}/workspace`

## 학생 채널 워크스페이스
- `GET /api/student/channels/{channelId}/workspace`

## 채널 실시간 이벤트
- SSE 구독: `GET /api/channels/{channelId}/events?accessToken=`
- 입장: `POST /api/channels/{channelId}/presence/enter`
- heartbeat: `POST /api/channels/{channelId}/presence/heartbeat`
- 퇴장: `POST /api/channels/{channelId}/presence/leave`
- 메시지 전송: `POST /api/channels/{channelId}/messages`
