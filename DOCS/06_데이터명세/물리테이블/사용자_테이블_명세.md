# users 테이블 명세

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| id | TEXT | PK | 사용자 ID |
| login_id | TEXT | UNIQUE | 로그인 ID |
| password_hash | TEXT | NOT NULL | 비밀번호 해시 |
| role | TEXT | NOT NULL | 역할 |
| display_name | TEXT | NOT NULL | 표시 이름 |
| school_id | TEXT | NOT NULL | 학교 tenant |
| classroom_id | TEXT | NULL | 학급 |
