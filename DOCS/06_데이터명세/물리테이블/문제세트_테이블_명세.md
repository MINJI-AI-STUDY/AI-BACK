# question_sets 테이블 명세

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| id | TEXT | PK | 문제 세트 ID |
| material_id | TEXT | NOT NULL | 자료 ID |
| teacher_id | TEXT | NOT NULL | 교사 ID |
| school_id | TEXT | NOT NULL | 학교 tenant |
| difficulty | TEXT | NOT NULL | 난이도 |
| status | TEXT | NOT NULL | REVIEW_REQUIRED/PUBLISHED/CLOSED |
| distribution_code | TEXT | NULL | 배포 코드 |
| due_at | DATETIME | NULL | 마감 시각 |
