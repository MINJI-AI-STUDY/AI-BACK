create table school_master (
    id varchar(64) primary key,
    official_school_code varchar(64) not null,
    name varchar(255) not null,
    school_level varchar(32) not null,
    address varchar(500),
    region varchar(100),
    email_domain varchar(255),
    active boolean not null default true,
    created_at timestamp not null,
    constraint uk_school_master_code unique (official_school_code)
);

create index idx_school_master_name on school_master(name);

insert into school_master (id, official_school_code, name, school_level, address, region, email_domain, active, created_at) values
('school-a', 'KOR-HIGH-0001', 'A고등학교', 'HIGH', '서울특별시', '서울', 'a-hs.kr', true, current_timestamp),
('school-b', 'KOR-HIGH-0002', 'B고등학교', 'HIGH', '부산광역시', '부산', 'b-hs.kr', true, current_timestamp);

create table school_operator_memberships (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    user_id varchar(64) not null,
    active boolean not null default true,
    created_at timestamp not null,
    constraint fk_school_operator_memberships_school foreign key (school_id) references schools(id),
    constraint fk_school_operator_memberships_user foreign key (user_id) references app_users(id)
);

create unique index uk_school_operator_membership on school_operator_memberships(school_id, user_id);

create table signup_requests (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    classroom_id varchar(64),
    requester_name varchar(255) not null,
    login_id varchar(100),
    password_hash varchar(255),
    role varchar(32) not null,
    school_email varchar(255),
    student_real_name varchar(255),
    status varchar(32) not null,
    consent_terms boolean not null,
    consent_privacy boolean not null,
    consent_student_notice boolean not null,
    rejection_reason varchar(1000),
    created_at timestamp not null,
    reviewed_at timestamp,
    reviewed_by varchar(64),
    constraint fk_signup_requests_school foreign key (school_id) references schools(id),
    constraint fk_signup_requests_classroom foreign key (classroom_id) references classrooms(id)
);

create index idx_signup_requests_school_status on signup_requests(school_id, status);

create table approval_audit_logs (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    signup_request_id varchar(64) not null,
    reviewer_user_id varchar(64) not null,
    action varchar(32) not null,
    note varchar(1000),
    created_at timestamp not null,
    constraint fk_approval_audit_logs_school foreign key (school_id) references schools(id),
    constraint fk_approval_audit_logs_request foreign key (signup_request_id) references signup_requests(id)
);

insert into school_operator_memberships (id, school_id, user_id, active, created_at) values
('school-op-a', 'school-a', 'operator-1', true, current_timestamp),
('school-op-b', 'school-b', 'operator-1', true, current_timestamp);
