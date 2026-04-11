# BACKEND_ROLLBACK

## 1. 목적
이 문서는 AI-BACK 장애 시 백엔드 기준 rollback 또는 forward-fix 판단 절차를 정의한다.

## 관련 문서
- 공통 rollback 런북: `../../../DOCS/10_운영/ROLLBACK_RUNBOOK.md`
- 백엔드 게이트: `BACKEND_RELEASE_GATE.md`
- 증거 템플릿: `../../../DOCS/10_운영/EVIDENCE_TEMPLATE.md`

## 2. 우선 분류
- 설정/secret 오류
- 코드 배포 오류
- migration 영향
- 외부 AI 연동 오류

## 3. 대응 순서
1. 설정 문제인지 코드 문제인지 분리한다.
2. auth / material / question / submission / qa 영향 범위를 확인한다.
3. 직전 정상 버전 복귀 가능 여부를 판단한다.
4. migration 적용 상태를 확인한다.
5. rollback 불가면 forward-fix 절차와 영향 범위를 남긴다.

## 4. 복구 후 확인
- 로그인 가능 여부
- 권한 경계 유지 여부
- 핵심 API smoke 재확인
- AI 연동 fallback 확인

## 5. 실제 복구 입력
- 직전 정상 백엔드 버전:
- Railway rollback 방법:
- migration 영향 메모:
- 복구 담당자:
