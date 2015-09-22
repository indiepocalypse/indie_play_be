# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table indie_user (
  id                        varchar(255) not null,
  indie_repos_with_shares_url varchar(255),
  indie_pull_requests_url   varchar(255),
  indie_home_html_url       varchar(255),
  indie_followers           integer,
  indie_following           integer,
  indie_followers_url       varchar(255),
  indie_following_url       varchar(255),
  indie_starred_url         varchar(255),
  constraint pk_indie_user primary key (id))
;

create table repo (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_repo primary key (id))
;

create sequence indie_user_seq;

create sequence repo_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists indie_user;

drop table if exists repo;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists indie_user_seq;

drop sequence if exists repo_seq;

