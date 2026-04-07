# materials 테이블 명세

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| id | TEXT | PK | 자료 ID |
| teacher_id | TEXT | NOT NULL | 교사 ID |
| school_id | TEXT | NOT NULL | 학교 tenant |
| title | TEXT | NOT NULL | 자료 제목 |
| description | TEXT | NULL | 자료 설명 |
| file_path | TEXT | NOT NULL | 저장 파일 경로 |
| status | TEXT | NOT NULL | 자료 상태 |
| failure_reason | TEXT | NULL | 실패 사유 |
