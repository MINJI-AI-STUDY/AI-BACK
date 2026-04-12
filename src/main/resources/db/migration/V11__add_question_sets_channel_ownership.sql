alter table question_sets add column channel_id varchar(64);

update question_sets
set channel_id = (
    select materials.channel_id
    from materials
    where materials.id = question_sets.material_id
)
where channel_id is null;

alter table question_sets add constraint fk_question_sets_channel foreign key (channel_id) references channels(id);
create index idx_question_sets_channel_id on question_sets(channel_id);
create index idx_question_sets_channel_status_created on question_sets(channel_id, status, created_at);
