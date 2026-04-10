# F6 자료 기반 AI 질의응답

## API 명세
- `POST /api/student/materials/{materialId}/qa`
- `GET /api/student/materials/{materialId}/qa-logs/me`
- `GET /api/teacher/materials/{materialId}/qa-logs`

## 정책

### POL-QA-001 질문 길이 정책
- 코드: `POL-QA-001`
- 내용: 질문은 공백 제거 후 1자 이상 500자 이하만 허용합니다.

### POL-QA-002 근거 응답 정책
- 코드: `POL-QA-002`
- 내용: 답변은 업로드된 자료를 기준으로 생성하고, 근거 snippet은 `rag_top_k` 범위에서 노출합니다.

### POL-QA-003 근거 부족 안내 정책
- 코드: `POL-QA-003`
- 내용: 자료에서 직접 근거를 찾기 어려우면 `insufficientEvidence=true`와 함께 안내 문구를 반환합니다.

### POL-QA-004 fallback 정책
- 코드: `POL-QA-004`
- 내용: AI 연동이 실패하면 자료 제목/추출 텍스트를 기반으로 한 fallback 답변으로 응답을 닫습니다.

### POL-QA-005 로그 저장 정책
- 코드: `POL-QA-005`
- 내용: 모든 질문/답변은 `materialId`, `studentId`, `question`, `answer`, `grounded`, `status`, `createdAt` 기준으로 저장합니다.

### POL-QA-006 형식 보장 정책
- 코드: `POL-QA-006`
- 내용: 답변은 `[답변]`, `[근거 요약]`, `[판단]` 섹션을 포함하도록 정규화합니다.

### POL-QA-007 도메인 차단 정책
- 코드: `POL-QA-007`
- 내용: 현재 업로드된 문서/학습 도메인과 무관한 질문은 차단 응답으로 처리합니다.
