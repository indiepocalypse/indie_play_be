# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table model_gmail_last_date_read (
  id                        varchar(255) not null,
  lastdate                  timestamp,
  constraint pk_model_gmail_last_date_read primary key (id))
;

create table model_merge_transaction (
  id                        varchar(255) not null,
  offer_id                  varchar(255),
  request_id                varchar(255),
  from_user_ownership_id    varchar(255),
  to_user_ownership_id      varchar(255),
  amount_percent            decimal(5,2),
  from_user_name            varchar(255),
  to_user_name              varchar(255),
  pull_request_id           varchar(255),
  repo_name                 varchar(255),
  from_user_ownership_percent decimal(5,2),
  date                      timestamp,
  constraint pk_model_merge_transaction primary key (id))
;

create table model_offer_for_merge (
  id                        varchar(255) not null,
  user_name                 varchar(255),
  amount_percent            decimal(5,2),
  user_ownership_percent    decimal(5,2),
  date_accepted_if_accepted timestamp,
  date_created              timestamp,
  issue_num                 varchar(255),
  repo_name                 varchar(255),
  pull_request_id           varchar(255),
  is_active                 boolean,
  was_positively_accepted   boolean,
  constraint pk_model_offer_for_merge primary key (id))
;

create table model_ownership (
  id                        varchar(255) not null,
  user_name                 varchar(255),
  repo_name                 varchar(255),
  percent                   decimal(5,2),
  is_creator                boolean,
  constraint pk_model_ownership primary key (id))
;

create table model_pull_request (
  id                        varchar(255) not null,
  number                    varchar(255),
  user_name                 varchar(255),
  sha                       varchar(255),
  repo_name                 varchar(255),
  title                     varchar(255),
  body                      varchar(255),
  merged                    boolean,
  mergeable                 boolean,
  url                       varchar(255),
  github_id                 bigint,
  html_url                  varchar(255),
  comments_url              varchar(255),
  state                     varchar(255),
  constraint pk_model_pull_request primary key (id))
;

create table model_repo (
  repo_name                 varchar(255) not null,
  repo_description          varchar(255),
  github_html_url           varchar(255),
  repo_homepage             varchar(255),
  stars_count               integer,
  forks_count               integer,
  constraint pk_model_repo primary key (repo_name))
;

create table model_repo_image (
  file_name                 varchar(255) not null,
  repo_name                 varchar(255),
  uploaded_date             timestamp,
  uploaded_by_user_name     varchar(255),
  image                     blob,
  constraint pk_model_repo_image primary key (file_name))
;

create table model_repo_policy (
  id                        varchar(255) not null,
  ownership_required_to_change_policy decimal(5,2),
  ownership_required_to_manage_issues decimal(5,2),
  ownership_required_to_merge_pull_requests decimal(5,2),
  ownership_required_to_manage_repo decimal(5,2),
  repo_name                 varchar(255),
  constraint pk_model_repo_policy primary key (id))
;

create table model_request_for_merge (
  id                        varchar(255) not null,
  user_name                 varchar(255),
  amount_percent            decimal(5,2),
  user_ownership_percent    decimal(5,2),
  date_accepted_if_accepted timestamp,
  date_created              timestamp,
  pull_request_id           varchar(255),
  is_active                 boolean,
  was_positively_accepted   boolean,
  constraint pk_model_request_for_merge primary key (id))
;

create table model_user (
  user_name                 varchar(255) not null,
  github_html_url           varchar(255),
  avatar_url                varchar(255),
  constraint pk_model_user primary key (user_name))
;

create table model_user_extended_info (
  id                        varchar(255) not null,
  user_name                 varchar(255),
  is_admin                  boolean,
  rate_limit_was_communicated_to_user_via_github_comment boolean,
  constraint pk_model_user_extended_info primary key (id))
;

create table model_user_interaction (
  id                        varchar(255) not null,
  user_name                 varchar(255),
  date_performed            timestamp,
  hook_interaction_type     integer,
  web_interaction_type      integer,
  mail_interaction_type     integer,
  p1                        varchar(255),
  p1_desc                   varchar(255),
  p2                        varchar(255),
  p2_desc                   varchar(255),
  p3                        varchar(255),
  p3_desc                   varchar(255),
  p4                        varchar(255),
  p4_desc                   varchar(255),
  p5                        varchar(255),
  p5_desc                   varchar(255),
  constraint ck_model_user_interaction_hook_interaction_type check (hook_interaction_type in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19)),
  constraint ck_model_user_interaction_web_interaction_type check (web_interaction_type in (0,1,2,3,4)),
  constraint ck_model_user_interaction_mail_interaction_type check (mail_interaction_type in (0,1)),
  constraint pk_model_user_interaction primary key (id))
;

create sequence model_gmail_last_date_read_seq;

create sequence model_merge_transaction_seq;

create sequence model_offer_for_merge_seq;

create sequence model_ownership_seq;

create sequence model_pull_request_seq;

create sequence model_repo_seq;

create sequence model_repo_image_seq;

create sequence model_repo_policy_seq;

create sequence model_request_for_merge_seq;

create sequence model_user_seq;

create sequence model_user_extended_info_seq;

create sequence model_user_interaction_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists model_gmail_last_date_read;

drop table if exists model_merge_transaction;

drop table if exists model_offer_for_merge;

drop table if exists model_ownership;

drop table if exists model_pull_request;

drop table if exists model_repo;

drop table if exists model_repo_image;

drop table if exists model_repo_policy;

drop table if exists model_request_for_merge;

drop table if exists model_user;

drop table if exists model_user_extended_info;

drop table if exists model_user_interaction;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists model_gmail_last_date_read_seq;

drop sequence if exists model_merge_transaction_seq;

drop sequence if exists model_offer_for_merge_seq;

drop sequence if exists model_ownership_seq;

drop sequence if exists model_pull_request_seq;

drop sequence if exists model_repo_seq;

drop sequence if exists model_repo_image_seq;

drop sequence if exists model_repo_policy_seq;

drop sequence if exists model_request_for_merge_seq;

drop sequence if exists model_user_seq;

drop sequence if exists model_user_extended_info_seq;

drop sequence if exists model_user_interaction_seq;

