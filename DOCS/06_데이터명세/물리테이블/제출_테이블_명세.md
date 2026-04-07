# submissions 테이블 명세

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| id | TEXT | PK | 제출 ID |
| question_set_id | TEXT | NOT NULL | 문제 세트 ID |
| student_id | TEXT | NOT NULL | 학생 ID |
| school_id | TEXT | NOT NULL | 학교 tenant |
| score | INTEGER | NOT NULL | 총점 |
| submitted_at | DATETIME | NOT NULL | 제출 시각 |
