# qna_logs 테이블 명세

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| id | TEXT | PK | 로그 ID |
| material_id | TEXT | NOT NULL | 자료 ID |
| student_id | TEXT | NOT NULL | 학생 ID |
| school_id | TEXT | NOT NULL | 학교 tenant |
| question | TEXT | NOT NULL | 질문 |
| answer | TEXT | NOT NULL | 답변 |
| grounded | BOOLEAN | NOT NULL | grounded 여부 |
| status | TEXT | NOT NULL | SUCCESS/INSUFFICIENT_EVIDENCE/FALLBACK |
| created_at | DATETIME | NOT NULL | 생성 시각 |
