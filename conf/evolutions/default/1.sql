# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table repo (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_repo primary key (id))
;

create sequence repo_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists repo;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists repo_seq;

