# questions 테이블 명세

| 컬럼명 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| id | TEXT | PK | 문항 ID |
| question_set_id | TEXT | NOT NULL | 문제 세트 ID |
| stem | TEXT | NOT NULL | 질문 본문 |
| option_a | TEXT | NOT NULL | 보기 A |
| option_b | TEXT | NOT NULL | 보기 B |
| option_c | TEXT | NOT NULL | 보기 C |
| option_d | TEXT | NOT NULL | 보기 D |
| correct_option_index | INTEGER | NOT NULL | 정답 인덱스 |
| explanation | TEXT | NOT NULL | 해설 |
| concept_tags | TEXT | NOT NULL | 태그 목록(JSON/CSV) |
| excluded | BOOLEAN | NOT NULL | 제외 여부 |
