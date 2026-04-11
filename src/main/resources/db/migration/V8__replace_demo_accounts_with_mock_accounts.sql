update app_users
set login_id = 'mock-teacher',
    display_name = '교사 목업',
    classroom_id = 'class-a'
where id = 'teacher-1';

update app_users
set login_id = 'mock-student',
    display_name = '학생 목업',
    classroom_id = 'class-a'
where id = 'student-1';

update app_users
set school_id = 'school-a',
    classroom_id = null,
    login_id = 'mock-operator',
    display_name = '학교 운영자 목업'
where id = 'operator-1';

delete from school_operator_memberships
where user_id = 'operator-1';

insert into school_operator_memberships (id, school_id, user_id, active, created_at)
values ('school-op-mock-a', 'school-a', 'operator-1', true, current_timestamp);
