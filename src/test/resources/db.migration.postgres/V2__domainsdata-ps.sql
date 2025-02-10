alter table runeventsprojectschema.domains add column created_by varchar(20) not null;
alter table runeventsprojectschema.domains add column updated_by varchar(20) not null default NULL;
alter table runeventsprojectschema.domain_events add column created_by varchar(20) not null;
alter table runeventsprojectschema.domain_events add column updated_by varchar(20) not null default NULL;
