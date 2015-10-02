# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table gmail_last_date_read (
  id                        varchar(255) not null,
  lastdate                  timestamp,
  constraint pk_gmail_last_date_read primary key (id))
;

create sequence gmail_last_date_read_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists gmail_last_date_read;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists gmail_last_date_read_seq;


