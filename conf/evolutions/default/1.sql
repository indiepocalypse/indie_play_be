# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table gmail_last_date_read (
  id                        varchar(255) not null,
  lastdate                  timestamp,
  constraint pk_gmail_last_date_read primary key (id))
;

create table ownership_model (
  id                        varchar(255) not null,
  user_user_name            varchar(255),
  repo_repo_name            varchar(255),
  percent                   decimal(38),
  constraint pk_ownership_model primary key (id))
;

create table repo_model (
  repo_name                 varchar(255) not null,
  repo_description          varchar(255),
  repo_homepage             varchar(255),
  github_html_url           varchar(255),
  stars_count               integer,
  forks_count               integer,
  constraint pk_repo_model primary key (repo_name))
;

create table user_model (
  user_name                 varchar(255) not null,
  user_blog_url             varchar(255),
  github_html_url           varchar(255),
  email                     varchar(255),
  avatar_url                varchar(255),
  public_repos              integer,
  github_repos_url          varchar(255),
  followers                 integer,
  following                 integer,
  constraint pk_user_model primary key (user_name))
;

create sequence gmail_last_date_read_seq;

create sequence ownership_model_seq;

create sequence repo_model_seq;

create sequence user_model_seq;

alter table ownership_model add constraint fk_ownership_model_user_1 foreign key (user_user_name) references user_model (user_name) on delete restrict on update restrict;
create index ix_ownership_model_user_1 on ownership_model (user_user_name);
alter table ownership_model add constraint fk_ownership_model_repo_2 foreign key (repo_repo_name) references repo_model (repo_name) on delete restrict on update restrict;
create index ix_ownership_model_repo_2 on ownership_model (repo_repo_name);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists gmail_last_date_read;

drop table if exists ownership_model;

drop table if exists repo_model;

drop table if exists user_model;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists gmail_last_date_read_seq;

drop sequence if exists ownership_model_seq;

drop sequence if exists repo_model_seq;

drop sequence if exists user_model_seq;

