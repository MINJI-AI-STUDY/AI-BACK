insert into schools (id, name, active, created_at) values
('school-a', 'A고등학교', true, current_timestamp),
('school-b', 'B고등학교', true, current_timestamp),
('operator-global', '운영자 조직', true, current_timestamp);

insert into classrooms (id, school_id, name, grade, created_at) values
('class-a', 'school-a', '1학년 1반', 1, current_timestamp),
('class-b', 'school-b', '1학년 1반', 1, current_timestamp),
('ops-room', 'operator-global', '운영자', null, current_timestamp);

insert into app_users (id, school_id, classroom_id, login_id, password, display_name, role, active, created_at) values
('teacher-1', 'school-a', 'class-a', 'teacher', '$2a$10$5WSc/dXNohMsF2hu/wiF/OkLrSZKP7kA8SJ.wKxRFYF9y6O4lpMwi', '교사 데모', 'TEACHER', true, current_timestamp),
('student-1', 'school-a', 'class-a', 'student', '$2a$10$PQGVKeyTEiO.CWm/2FWN1e1zZcodbIchDDLpO0cYkdcZqA5GfwwZ2', '학생 데모', 'STUDENT', true, current_timestamp),
('teacher-2', 'school-b', 'class-b', 'teacher2', '$2a$10$5WSc/dXNohMsF2hu/wiF/OkLrSZKP7kA8SJ.wKxRFYF9y6O4lpMwi', '교사 데모 2', 'TEACHER', true, current_timestamp),
('student-2', 'school-b', 'class-b', 'student2', '$2a$10$PQGVKeyTEiO.CWm/2FWN1e1zZcodbIchDDLpO0cYkdcZqA5GfwwZ2', '학생 데모 2', 'STUDENT', true, current_timestamp),
('operator-1', 'operator-global', 'ops-room', 'operator', '$2a$10$HZVOzINU8y/9sLzYWEz7vuSa6rXW4.6SRxJm2mYrWhF9KQUHLpuXW', '운영자 데모', 'OPERATOR', true, current_timestamp);
