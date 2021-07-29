-- this migration plan is prepared for new database schema created on EN-146
-- and should be used only for this migration


-- NEW TABLE CREATION


-- entire context of conversion task, this context uses conversion data, and should be created before
-- conversion_data relation was created, as it has dependency on it
create table conversion_task_context
(
    id                                bigint not null
        primary key,
    task_state                        int null,
    record_id                         bigint null,
    has_added_failure                 bit    not null,
    has_converted                     bit    not null,
    has_converter_converted_toiiif    bit    not null,
    has_converter_created_data_holder bit    not null,
    has_converter_saved_files         bit    not null,
    has_thrown_exception              bit    not null,
    record_json                       longtext null,
    record_json_raw                   longtext null,
    constraint UK_qwfgbudr8tm79eoc525fap3nd
        unique (record_id)
) engine = MyISAM;

-- here we store data for converter
create table conversion_data
(
    id                         bigint auto_increment
        primary key,
    json                       varchar(255) null,
    media_type                 varchar(255) null,
    src_file_path              varchar(255) null,
    src_file_url               varchar(255) null,
    conversion_task_context_id bigint null
) engine = MyISAM;

create index FKj0jkmnyvulf3cprb398xglp1y
    on conversion_data (conversion_task_context_id);

-- here we store dimension of each image downloaded for conversion data
create table conversion_data_dimension
(
    conversion_data_id bigint not null,
    height             int    not null,
    width              int    not null
) engine = MyISAM;

create index FK5nl06ipwni4p5k72m3jrthmey
    on conversion_data_dimension (conversion_data_id);

-- here we store paths for images downloaded and used by conversion data
create table conversion_data_image_path
(
    conversion_data_id bigint not null,
    image_path         varchar(255) null
) engine = MyISAM;

create index FKq4qp6415k9s7jo6hjxkjqpx8n
    on conversion_data_image_path (conversion_data_id);


-- here path to images after conversion to iiif format
create table conversion_data_out_file_path
(
    conversion_data_id bigint not null,
    out_file_path      varchar(255) null
) engine = MyISAM;

create index FK1gps9jp76y93gdm3dn60jy2xk
    on conversion_data_out_file_path (conversion_data_id);


-- enrich task table, should be created before enrich task context -> saved transcription mapping
create table enrich_task_context
(
    id                        bigint not null
        primary key,
    task_state                int null,
    record_id                 bigint null,
    has_downloaded_enrichment bit    not null,
    constraint UK_7qo0hlr7qgc7nqtp311d86f5f
        unique (record_id)
) engine = MyISAM;

-- this relation stores information which transcriptions have been downloaded so far
-- this could be done via normal Transcription relation, but we do like to put in DB only
-- full set of Transcriptions, and not some portion of it, thus we create new relation as temporary
-- place for them
create table enrich_task_context_saved_transcriptions
(
    enrich_task_context_id  bigint not null,
    saved_transcriptions_id bigint not null,
    constraint UK_4pgqvq3orq1tedwxpxnwd7xnu
        unique (saved_transcriptions_id)
) engine = MyISAM;


-- here we store exception thrown during task processing
create table persistable_exception_entity
(
    id         bigint auto_increment
        primary key,
    message    text null,
    type       varchar(255) null,
    context_id bigint null
) engine = MyISAM;


-- here context of transcription task. Has no deps on other relations, can be created anytime
create table transcribe_task_context
(
    id                bigint not null
        primary key,
    task_state        int null,
    record_id         bigint null,
    has_added_failure bit    not null,
    has_send_record   bit    not null,
    has_thrown_error  bit    not null,
    record_json       longtext null,
    record_json_raw   longtext null,
    constraint UK_kiqfan5kgnlppn6393mut42k0
        unique (record_id)
) engine = MyISAM;

-- in this table are stored update task contexts
create table update_task_context
(
    id                                 bigint not null
        primary key,
    task_state                         int null,
    record_id                          bigint null,
    has_fetched_updated_transcriptions bit    not null,
    has_send_updates                   bit    not null,
    transcription_id                   bigint null,
    constraint UK_fhm1ro51vm5dxhwceudmv7gkl
        unique (record_id)
) engine = MyISAM;

create index FK3mc0r977wrvom35ayyr2stwp5
    on update_task_context (transcription_id);

-- ALTERING OLD TABLES


-- for easier recovery DB will store transcriptions context to not download it at recovery time
alter table transcription
    add transcription_content longtext null;