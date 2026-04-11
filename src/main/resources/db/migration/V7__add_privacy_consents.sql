create table privacy_consents (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    consent_type varchar(64) not null,
    consented boolean not null default false,
    consented_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_privacy_consents_user foreign key (user_id) references app_users(id),
    constraint uk_privacy_consents_user_type unique (user_id, consent_type)
);

create index idx_privacy_consents_user on privacy_consents(user_id);