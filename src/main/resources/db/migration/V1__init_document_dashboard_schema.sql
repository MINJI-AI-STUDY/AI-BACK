create table materials (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    teacher_id varchar(64) not null,
    doc_no bigint not null,
    title varchar(255) not null,
    description text,
    original_file_name varchar(255) not null,
    file_path varchar(1000) not null,
    status varchar(32) not null,
    failure_reason varchar(1000),
    extracted_text text,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_material_school_doc_no unique (school_id, doc_no)
);

create index idx_material_school_id on materials(school_id);

create table question_sets (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    material_id varchar(64) not null,
    teacher_id varchar(64) not null,
    difficulty varchar(32) not null,
    status varchar(32) not null,
    distribution_code varchar(32),
    distribution_link varchar(255),
    due_at timestamp,
    created_at timestamp not null,
    constraint fk_question_sets_material foreign key (material_id) references materials(id)
);

create index idx_question_sets_material_id on question_sets(material_id);
create index idx_question_sets_school_id on question_sets(school_id);
create unique index uk_question_sets_distribution_code on question_sets(distribution_code);

create table questions (
    id varchar(64) primary key,
    question_set_id varchar(64) not null,
    stem text not null,
    correct_option_index integer not null,
    explanation text not null,
    excluded boolean not null,
    constraint fk_questions_question_set foreign key (question_set_id) references question_sets(id)
);

create table question_options (
    question_id varchar(64) not null,
    option_order integer not null,
    option_value varchar(1000) not null,
    constraint fk_question_options_question foreign key (question_id) references questions(id)
);

create table question_concept_tags (
    question_id varchar(64) not null,
    tag_order integer not null,
    tag_value varchar(255) not null,
    constraint fk_question_concept_tags_question foreign key (question_id) references questions(id)
);

create table submissions (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    material_id varchar(64) not null,
    question_set_id varchar(64) not null,
    student_id varchar(64) not null,
    score integer not null,
    submitted_at timestamp not null,
    constraint fk_submissions_material foreign key (material_id) references materials(id),
    constraint fk_submissions_question_set foreign key (question_set_id) references question_sets(id)
);

create index idx_submissions_question_set_id on submissions(question_set_id);
create index idx_submissions_material_student on submissions(material_id, student_id);

create table submission_answer_results (
    id varchar(64) primary key,
    submission_id varchar(64) not null,
    question_id varchar(64) not null,
    selected_option_index integer not null,
    correct boolean not null,
    explanation text not null,
    constraint fk_submission_answer_results_submission foreign key (submission_id) references submissions(id)
);

create table submission_answer_result_tags (
    submission_answer_result_id varchar(64) not null,
    tag_order integer not null,
    tag_value varchar(255) not null,
    constraint fk_submission_answer_result_tags_result foreign key (submission_answer_result_id) references submission_answer_results(id)
);

create table qa_logs (
    id varchar(64) primary key,
    school_id varchar(64) not null,
    material_id varchar(64) not null,
    student_id varchar(64) not null,
    question text not null,
    answer text not null,
    grounded boolean not null,
    status varchar(64) not null,
    created_at timestamp not null,
    constraint fk_qa_logs_material foreign key (material_id) references materials(id)
);

create index idx_qa_logs_material_student_created_at on qa_logs(material_id, student_id, created_at desc);

create table qa_log_evidence_snippets (
    qa_log_id varchar(64) not null,
    snippet_order integer not null,
    snippet_value text not null,
    constraint fk_qa_log_evidence_snippets_log foreign key (qa_log_id) references qa_logs(id)
);
