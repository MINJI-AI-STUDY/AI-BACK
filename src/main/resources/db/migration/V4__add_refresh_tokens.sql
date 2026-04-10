create table refresh_tokens (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    token varchar(255) not null,
    expires_at timestamp not null,
    revoked boolean not null default false,
    created_at timestamp not null,
    constraint fk_refresh_tokens_user foreign key (user_id) references app_users(id),
    constraint uk_refresh_tokens_token unique (token)
);

create index idx_refresh_tokens_user_id on refresh_tokens(user_id);
