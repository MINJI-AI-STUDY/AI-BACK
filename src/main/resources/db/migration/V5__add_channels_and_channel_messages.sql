create table channels (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    name varchar(255) not null,
    description varchar(1000),
    sort_order integer not null,
    active boolean not null default true,
    created_by varchar(64) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_channels_school foreign key (school_id) references schools(id)
);

create index idx_channels_school_active on channels(school_id, active, sort_order);

insert into channels (id, school_id, name, description, sort_order, active, created_by, created_at, updated_at) values
('school-a-channel-1','school-a','공지','학교 공지 채널',1,true,'teacher-1',current_timestamp,current_timestamp),
('school-a-channel-2','school-a','수학-1','수학 학습 채널 1',2,true,'teacher-1',current_timestamp,current_timestamp),
('school-a-channel-3','school-a','수학-2','수학 학습 채널 2',3,true,'teacher-1',current_timestamp,current_timestamp),
('school-a-channel-4','school-a','과제','과제 공유 채널',4,true,'teacher-1',current_timestamp,current_timestamp),
('school-a-channel-5','school-a','질문','질문 전용 채널',5,true,'teacher-1',current_timestamp,current_timestamp),
('school-a-channel-6','school-a','심화','심화 학습 채널',6,true,'teacher-1',current_timestamp,current_timestamp),
('school-a-channel-7','school-a','복습','복습 채널',7,true,'teacher-1',current_timestamp,current_timestamp),
('school-a-channel-8','school-a','시험대비','시험 대비 채널',8,true,'teacher-1',current_timestamp,current_timestamp),
('school-a-channel-9','school-a','프로젝트','프로젝트 채널',9,true,'teacher-1',current_timestamp,current_timestamp),
('school-a-channel-10','school-a','자유토론','자유 토론 채널',10,true,'teacher-1',current_timestamp,current_timestamp),
('school-b-channel-1','school-b','공지','학교 공지 채널',1,true,'teacher-2',current_timestamp,current_timestamp),
('school-b-channel-2','school-b','수학-1','수학 학습 채널 1',2,true,'teacher-2',current_timestamp,current_timestamp),
('school-b-channel-3','school-b','수학-2','수학 학습 채널 2',3,true,'teacher-2',current_timestamp,current_timestamp),
('school-b-channel-4','school-b','과제','과제 공유 채널',4,true,'teacher-2',current_timestamp,current_timestamp),
('school-b-channel-5','school-b','질문','질문 전용 채널',5,true,'teacher-2',current_timestamp,current_timestamp),
('school-b-channel-6','school-b','심화','심화 학습 채널',6,true,'teacher-2',current_timestamp,current_timestamp),
('school-b-channel-7','school-b','복습','복습 채널',7,true,'teacher-2',current_timestamp,current_timestamp),
('school-b-channel-8','school-b','시험대비','시험 대비 채널',8,true,'teacher-2',current_timestamp,current_timestamp),
('school-b-channel-9','school-b','프로젝트','프로젝트 채널',9,true,'teacher-2',current_timestamp,current_timestamp),
('school-b-channel-10','school-b','자유토론','자유 토론 채널',10,true,'teacher-2',current_timestamp,current_timestamp);

alter table materials add column channel_id varchar(64);
alter table materials add constraint fk_materials_channel foreign key (channel_id) references channels(id);
create index idx_materials_channel_id on materials(channel_id);

update materials
set channel_id = case
    when school_id = 'school-a' then 'school-a-channel-1'
    when school_id = 'school-b' then 'school-b-channel-1'
    else null
end
where channel_id is null;

create table channel_messages (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    channel_id varchar(64) not null,
    user_id varchar(64) not null,
    display_name varchar(255) not null,
    role varchar(32) not null,
    content text not null,
    created_at timestamp not null,
    constraint fk_channel_messages_channel foreign key (channel_id) references channels(id)
);

create index idx_channel_messages_channel_created on channel_messages(channel_id, created_at);
