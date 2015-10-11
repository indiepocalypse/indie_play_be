# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table model_gmail_last_date_read (
  id                        varchar(255) not null,
  lastdate                  timestamp,
  constraint pk_model_gmail_last_date_read primary key (id))
;

create table model_ownership (
  id                        varchar(255) not null,
  user_user_name            varchar(255),
  repo_repo_name            varchar(255),
  percent                   decimal(38),
  constraint pk_model_ownership primary key (id))
;

create table model_repo (
  repo_name                 varchar(255) not null,
  repo_description          varchar(255),
  repo_homepage             varchar(255),
  github_html_url           varchar(255),
  stars_count               integer,
  forks_count               integer,
  constraint pk_model_repo primary key (repo_name))
;

create table model_user (
  user_name                 varchar(255) not null,
  user_blog_url             varchar(255),
  github_html_url           varchar(255),
  email                     varchar(255),
  avatar_url                varchar(255),
  public_repos              integer,
  github_repos_url          varchar(255),
  followers                 integer,
  following                 integer,
  constraint pk_model_user primary key (user_name))
;

create sequence model_gmail_last_date_read_seq;

create sequence model_ownership_seq;

create sequence model_repo_seq;

create sequence model_user_seq;

alter table model_ownership add constraint fk_model_ownership_user_1 foreign key (user_user_name) references model_user (user_name) on delete restrict on update restrict;
create index ix_model_ownership_user_1 on model_ownership (user_user_name);
alter table model_ownership add constraint fk_model_ownership_repo_2 foreign key (repo_repo_name) references model_repo (repo_name) on delete restrict on update restrict;
create index ix_model_ownership_repo_2 on model_ownership (repo_repo_name);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists model_gmail_last_date_read;

drop table if exists model_ownership;

drop table if exists model_repo;

drop table if exists model_user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists model_gmail_last_date_read_seq;

drop sequence if exists model_ownership_seq;

drop sequence if exists model_repo_seq;

drop sequence if exists model_user_seq;

