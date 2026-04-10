create table schools (
    id varchar(64) primary key,
    name varchar(255) not null,
    active boolean not null default true,
    created_at timestamp not null
);

create table classrooms (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    name varchar(255) not null,
    grade integer,
    created_at timestamp not null,
    constraint fk_classrooms_school foreign key (school_id) references schools(id)
);

create table app_users (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    classroom_id varchar(64),
    login_id varchar(100) not null,
    password varchar(255) not null,
    display_name varchar(255) not null,
    role varchar(32) not null,
    active boolean not null default true,
    created_at timestamp not null,
    constraint fk_app_users_school foreign key (school_id) references schools(id),
    constraint fk_app_users_classroom foreign key (classroom_id) references classrooms(id),
    constraint uk_app_users_login_id unique (login_id)
);

create index idx_app_users_school_role on app_users(school_id, role);
