# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table social_profile_logins (
  id                        bigint not null,
  login                     varchar(255),
  profile_id                bigint,
  constraint pk_social_profile_logins primary key (id))
;

create table manufacturer (
  id                        bigint not null,
  name                      varchar(255),
  modified_at               timestamp not null,
  constraint pk_manufacturer primary key (id))
;

create table multimedia (
  id                        bigint not null,
  file_name                 varchar(255),
  url                       varchar(255),
  cdn                       integer,
  modified_at               timestamp not null,
  constraint ck_multimedia_cdn check (cdn in (0)),
  constraint pk_multimedia primary key (id))
;

create table product (
  id                        bigint not null,
  name                      varchar(255),
  nick_name                 varchar(255),
  manufacturer_id           bigint,
  multimedia                bigint,
  modified_at               timestamp not null,
  constraint pk_product primary key (id))
;

create table social_profile (
  id                        bigint not null,
  provider                  integer,
  access_token              varchar(255),
  user_id                   bigint,
  status                    integer,
  modified_at               timestamp not null,
  constraint ck_social_profile_provider check (provider in (0,1,2)),
  constraint ck_social_profile_status check (status in (0,1,2)),
  constraint pk_social_profile primary key (id))
;

create table token (
  content                   varchar(255) not null,
  user_id                   bigint,
  since                     timestamp not null,
  constraint pk_token primary key (content))
;

create table users (
  id                        bigint not null,
  login                     varchar(255),
  password                  varchar(255),
  mail                      varchar(255),
  name                      varchar(255),
  birthday                  timestamp,
  since                     timestamp,
  gender                    integer,
  status                    integer,
  modified_at               timestamp not null,
  constraint ck_users_gender check (gender in (0,1)),
  constraint ck_users_status check (status in (0,1,2,3)),
  constraint pk_users primary key (id))
;

create table user_mail_interaction (
  id                        bigint not null,
  status                    integer,
  hash                      varchar(255),
  mail                      varchar(255),
  user_id                   bigint,
  modified_at               timestamp,
  constraint ck_user_mail_interaction_status check (status in (0,1,2)),
  constraint pk_user_mail_interaction primary key (id))
;

create table wishlist (
  id                        bigint not null,
  title                     varchar(255),
  description               varchar(255),
  user_id                   bigint,
  status                    integer,
  modified_at               timestamp not null,
  constraint ck_wishlist_status check (status in (0,1,2)),
  constraint pk_wishlist primary key (id))
;

create table wishlist_product (
  id                        bigint not null,
  wishlist_id               bigint,
  product_id                bigint,
  status                    integer,
  constraint ck_wishlist_product_status check (status in (0,1,2)),
  constraint pk_wishlist_product primary key (id))
;

create sequence social_profile_logins_id_seq;

create sequence manufacturer_id_seq;

create sequence multimedia_id_seq;

create sequence product_id_seq;

create sequence social_profile_id_seq;

create sequence token_id_seq;

create sequence user_id_seq;

create sequence user_mail_interaction_id_seq;

create sequence wishlist_id_seq;

create sequence wishlist_product_id_seq;

alter table social_profile_logins add constraint fk_social_profile_logins_profi_1 foreign key (profile_id) references social_profile (id);
create index ix_social_profile_logins_profi_1 on social_profile_logins (profile_id);
alter table product add constraint fk_product_manufacturer_2 foreign key (manufacturer_id) references manufacturer (id);
create index ix_product_manufacturer_2 on product (manufacturer_id);
alter table product add constraint fk_product_multimedia_3 foreign key (multimedia) references multimedia (id);
create index ix_product_multimedia_3 on product (multimedia);
alter table social_profile add constraint fk_social_profile_user_4 foreign key (user_id) references users (id);
create index ix_social_profile_user_4 on social_profile (user_id);
alter table token add constraint fk_token_user_5 foreign key (user_id) references users (id);
create index ix_token_user_5 on token (user_id);
alter table user_mail_interaction add constraint fk_user_mail_interaction_user_6 foreign key (user_id) references users (id);
create index ix_user_mail_interaction_user_6 on user_mail_interaction (user_id);
alter table wishlist add constraint fk_wishlist_user_7 foreign key (user_id) references users (id);
create index ix_wishlist_user_7 on wishlist (user_id);
alter table wishlist_product add constraint fk_wishlist_product_wishList_8 foreign key (wishlist_id) references wishlist (id);
create index ix_wishlist_product_wishList_8 on wishlist_product (wishlist_id);
alter table wishlist_product add constraint fk_wishlist_product_product_9 foreign key (product_id) references product (id);
create index ix_wishlist_product_product_9 on wishlist_product (product_id);



# --- !Downs

drop table if exists social_profile_logins cascade;

drop table if exists manufacturer cascade;

drop table if exists multimedia cascade;

drop table if exists product cascade;

drop table if exists social_profile cascade;

drop table if exists token cascade;

drop table if exists users cascade;

drop table if exists user_mail_interaction cascade;

drop table if exists wishlist cascade;

drop table if exists wishlist_product cascade;

drop sequence if exists social_profile_logins_id_seq;

drop sequence if exists manufacturer_id_seq;

drop sequence if exists multimedia_id_seq;

drop sequence if exists product_id_seq;

drop sequence if exists social_profile_id_seq;

drop sequence if exists token_id_seq;

drop sequence if exists user_id_seq;

drop sequence if exists user_mail_interaction_id_seq;

drop sequence if exists wishlist_id_seq;

drop sequence if exists wishlist_product_id_seq;

