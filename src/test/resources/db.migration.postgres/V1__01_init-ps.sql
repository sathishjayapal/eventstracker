Drop sequence if exists runeventsprojectschema.event_id_seq;
Drop sequence if exists runeventsprojectschema.domain_id_seq;
Drop table if exists runeventsprojectschema.domain_events;
drop table if exists runeventsprojectschema.domains;

create sequence  runeventsprojectschema.event_id_seq start with 1 increment by 50;
create sequence  runeventsprojectschema.domain_id_seq start with 1 increment by 50;

create table  runeventsprojectschema.domains
(
    id                        bigint default nextval('runeventsprojectschema.domain_id_seq') not null,
    domain_name               text not null unique,
    status                    text not null,
    comments                  text,
    created_at                timestamp,
    updated_at                timestamp,
    primary key (id)
);


create table  runeventsprojectschema.domain_events
(
    id           bigint default nextval('runeventsprojectschema.event_id_seq') not null,
    domain_Name text                                          not null references runeventsprojectschema.domains(domain_name),
    event_id     text                                         not null unique,
    event_type   text                                         not null,
    payload      text                                         not null,
    created_at   timestamp                                    not null,
    updated_at   timestamp                                    not null,
    primary key (id)
);

