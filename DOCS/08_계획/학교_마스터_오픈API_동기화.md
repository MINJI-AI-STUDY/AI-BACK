# 학교 마스터 Open API 동기화

## 목적
- 전국 초중고 학교를 내부 `school_master` 테이블에 적재할 수 있는 경로를 제공합니다.

## 입력 환경변수
- `APP_SCHOOL_API_BASE_URL`
- `APP_SCHOOL_API_KEY`
- `APP_SCHOOL_API_ENDPOINT`
- `APP_SCHOOL_API_PAGE_SIZE`

## 동기화 엔드포인트
- `POST /api/operator/schools/sync-master`

## 기대 동작
- Open API에서 페이지 단위로 학교 데이터를 읽어옵니다.
- `official_school_code` 기준으로 upsert 합니다.
- 결과로 `importedCount`, `updatedCount`, `totalCount`를 반환합니다.

## 현재 한계
- 실제 전국 적재는 운영 환경의 유효한 Open API 키가 필요합니다.
- 현재 로컬/테스트는 sync 경로만 구현되어 있으며, sample seed는 여전히 포함됩니다.
