# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table repo (
  name                      varchar(255) not null,
  github_url                varchar(255),
  stars                     integer,
  description               varchar(255),
  constraint pk_repo primary key (name))
;

create table user (
  name                      varchar(255) not null,
  email                     varchar(255),
  constraint pk_user primary key (name))
;

create sequence repo_seq;

create sequence user_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists repo;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists repo_seq;

drop sequence if exists user_seq;

