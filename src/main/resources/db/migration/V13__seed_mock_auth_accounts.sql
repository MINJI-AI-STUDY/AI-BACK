-- 로컬/초기 환경에서 바로 사용할 목계정을 고정합니다.
-- teacher123 / operator123 / student123 해시를 기준으로 로그인 값을 재정렬합니다.

UPDATE schools
SET name = 'A고등학교',
    active = true
WHERE id = 'school-a';

UPDATE classrooms
SET school_id = 'school-a',
    name = '1학년 1반',
    grade = 1
WHERE id = 'class-a';

UPDATE app_users
SET school_id = 'school-a',
    classroom_id = 'class-a',
    login_id = 'mock-teacher',
    password = '$2a$10$5WSc/dXNohMsF2hu/wiF/OkLrSZKP7kA8SJ.wKxRFYF9y6O4lpMwi',
    display_name = '교사 목업',
    role = 'TEACHER',
    active = true
WHERE id = 'teacher-1';

UPDATE app_users
SET school_id = 'school-a',
    classroom_id = 'class-a',
    login_id = 'mock-student',
    password = '$2a$10$PQGVKeyTEiO.CWm/2FWN1e1zZcodbIchDDLpO0cYkdcZqA5GfwwZ2',
    pin = '$2a$10$PQGVKeyTEiO.CWm/2FWN1e1zZcodbIchDDLpO0cYkdcZqA5GfwwZ2',
    display_name = '학생 목업',
    role = 'STUDENT',
    active = true,
    student_code = 'S001'
WHERE id = 'student-1';

UPDATE app_users
SET school_id = 'school-a',
    classroom_id = null,
    login_id = 'mock-operator',
    password = '$2a$10$HZVOzINU8y/9sLzYWEz7vuSa6rXW4.6SRxJm2mYrWhF9KQUHLpuXW',
    display_name = '학교 운영자 목업',
    role = 'OPERATOR',
    active = true
WHERE id = 'operator-1';

DELETE FROM school_operator_memberships
WHERE user_id = 'operator-1';

INSERT INTO school_operator_memberships (id, school_id, user_id, active, created_at)
VALUES ('school-op-mock-a', 'school-a', 'operator-1', true, current_timestamp);
