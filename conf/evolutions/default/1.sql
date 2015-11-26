# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table model_admin (
  id                        varchar(255) not null,
  user_user_name            varchar(255),
  constraint pk_model_admin primary key (id))
;

create table model_gmail_last_date_read (
  id                        varchar(255) not null,
  lastdate                  timestamp,
  constraint pk_model_gmail_last_date_read primary key (id))
;

create table model_offer_for_merge (
  id                        varchar(255) not null,
  user_user_name            varchar(255),
  pull_request_id           varchar(255),
  amount_percent            decimal(5,2),
  date_accepted_if_accepted timestamp,
  date_created              timestamp,
  is_active                 boolean,
  was_positively_accepted   boolean,
  constraint pk_model_offer_for_merge primary key (id))
;

create table model_ownership (
  id                        varchar(255) not null,
  user_user_name            varchar(255),
  repo_repo_name            varchar(255),
  percent                   decimal(5,2),
  is_creator                boolean,
  constraint pk_model_ownership primary key (id))
;

create table model_pull_request (
  id                        varchar(255) not null,
  url                       varchar(255),
  github_id                 bigint,
  html_url                  varchar(255),
  number                    varchar(255),
  user_user_name            varchar(255),
  comments_url              varchar(255),
  sha                       varchar(255),
  repo_repo_name            varchar(255),
  title                     varchar(255),
  body                      varchar(255),
  state                     varchar(255),
  merged                    boolean,
  mergeable                 boolean,
  constraint pk_model_pull_request primary key (id))
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

create table model_repo_policy (
  id                        varchar(255) not null,
  repo_repo_name            varchar(255),
  ownership_required_to_change_policy decimal(5,2),
  ownership_required_to_manage_issues decimal(5,2),
  ownership_required_to_merge_pull_requests decimal(5,2),
  constraint pk_model_repo_policy primary key (id))
;

create table model_request_for_merge (
  id                        varchar(255) not null,
  user_user_name            varchar(255),
  pull_request_id           varchar(255),
  amount_percent            decimal(5,2),
  date_accepted_if_accepted timestamp,
  date_created              timestamp,
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

create sequence model_admin_seq;

create sequence model_gmail_last_date_read_seq;

create sequence model_offer_for_merge_seq;

create sequence model_ownership_seq;

create sequence model_pull_request_seq;

create sequence model_repo_seq;

create sequence model_repo_policy_seq;

create sequence model_request_for_merge_seq;

create sequence model_user_seq;

alter table model_admin add constraint fk_model_admin_user_1 foreign key (user_user_name) references model_user (user_name) on delete restrict on update restrict;
create index ix_model_admin_user_1 on model_admin (user_user_name);
alter table model_offer_for_merge add constraint fk_model_offer_for_merge_user_2 foreign key (user_user_name) references model_user (user_name) on delete restrict on update restrict;
create index ix_model_offer_for_merge_user_2 on model_offer_for_merge (user_user_name);
alter table model_offer_for_merge add constraint fk_model_offer_for_merge_pull__3 foreign key (pull_request_id) references model_pull_request (id) on delete restrict on update restrict;
create index ix_model_offer_for_merge_pull__3 on model_offer_for_merge (pull_request_id);
alter table model_ownership add constraint fk_model_ownership_user_4 foreign key (user_user_name) references model_user (user_name) on delete restrict on update restrict;
create index ix_model_ownership_user_4 on model_ownership (user_user_name);
alter table model_ownership add constraint fk_model_ownership_repo_5 foreign key (repo_repo_name) references model_repo (repo_name) on delete restrict on update restrict;
create index ix_model_ownership_repo_5 on model_ownership (repo_repo_name);
alter table model_pull_request add constraint fk_model_pull_request_user_6 foreign key (user_user_name) references model_user (user_name) on delete restrict on update restrict;
create index ix_model_pull_request_user_6 on model_pull_request (user_user_name);
alter table model_pull_request add constraint fk_model_pull_request_repo_7 foreign key (repo_repo_name) references model_repo (repo_name) on delete restrict on update restrict;
create index ix_model_pull_request_repo_7 on model_pull_request (repo_repo_name);
alter table model_repo_policy add constraint fk_model_repo_policy_repo_8 foreign key (repo_repo_name) references model_repo (repo_name) on delete restrict on update restrict;
create index ix_model_repo_policy_repo_8 on model_repo_policy (repo_repo_name);
alter table model_request_for_merge add constraint fk_model_request_for_merge_use_9 foreign key (user_user_name) references model_user (user_name) on delete restrict on update restrict;
create index ix_model_request_for_merge_use_9 on model_request_for_merge (user_user_name);
alter table model_request_for_merge add constraint fk_model_request_for_merge_pu_10 foreign key (pull_request_id) references model_pull_request (id) on delete restrict on update restrict;
create index ix_model_request_for_merge_pu_10 on model_request_for_merge (pull_request_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists model_admin;

drop table if exists model_gmail_last_date_read;

drop table if exists model_offer_for_merge;

drop table if exists model_ownership;

drop table if exists model_pull_request;

drop table if exists model_repo;

drop table if exists model_repo_policy;

drop table if exists model_request_for_merge;

drop table if exists model_user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists model_admin_seq;

drop sequence if exists model_gmail_last_date_read_seq;

drop sequence if exists model_offer_for_merge_seq;

drop sequence if exists model_ownership_seq;

drop sequence if exists model_pull_request_seq;

drop sequence if exists model_repo_seq;

drop sequence if exists model_repo_policy_seq;

drop sequence if exists model_request_for_merge_seq;

drop sequence if exists model_user_seq;

