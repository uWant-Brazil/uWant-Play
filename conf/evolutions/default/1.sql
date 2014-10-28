# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table actions (
  id                        bigint not null,
  type                      integer not null,
  from_id                   bigint,
  user_id                   bigint,
  extra                     varchar(255),
  created_at                timestamp not null,
  modified_at               timestamp not null,
  constraint ck_actions_type check (type in (0,1,2,3,4,5,6,7,8)),
  constraint pk_actions primary key (id))
;

create table actions_report (
  id                        bigint not null,
  action_id                 bigint,
  since                     timestamp without time zone default now() not null,
  constraint pk_actions_report primary key (id))
;

create table action_shares (
  id                        bigint not null,
  action_id                 bigint,
  user_id                   bigint,
  constraint pk_action_shares primary key (id))
;

create table administrators (
  id                        bigint not null,
  login                     varchar(255) not null,
  name                      varchar(255),
  mail                      varchar(255),
  status                    integer,
  constraint ck_administrators_status check (status in (0,1,2)),
  constraint uq_administrators_login unique (login),
  constraint pk_administrators primary key (id))
;

create table action_comments (
  id                        bigint not null,
  text                      varchar(255) not null,
  action_id                 bigint,
  user_id                   bigint,
  since                     timestamp not null,
  constraint pk_action_comments primary key (id))
;

create table friends_circle (
  requester_id              bigint,
  target_id                 bigint,
  is_blocked                BOOLEAN DEFAULT false not null,
  constraint pk_friends_circle primary key (requester_id, target_id))
;

create table manufacturer (
  id                        bigint not null,
  name                      varchar(255),
  modified_at               timestamp not null,
  constraint pk_manufacturer primary key (id))
;

create table user_mobiles (
  id                        bigint not null,
  identifier                varchar(255) not null,
  user_id                   bigint,
  token_id                  bigint,
  os                        integer,
  modified_at               timestamp not null,
  constraint ck_user_mobiles_os check (os in (0,1,2)),
  constraint uq_user_mobiles_identifier unique (identifier),
  constraint pk_user_mobiles primary key (id))
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

create table user_notifications (
  id                        bigint not null,
  delivered                 boolean,
  title                     varchar(255) not null,
  message                   varchar(255) not null,
  unique_identifier         varchar(255) not null,
  service_identifier        varchar(255),
  extra                     varchar(255),
  user_id                   bigint,
  action_id                 bigint,
  modified_at               timestamp not null,
  constraint pk_user_notifications primary key (id))
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

create table roles (
  id                        bigint not null,
  name                      varchar(255) not null,
  constraint pk_roles primary key (id))
;

create table social_profile (
  id                        bigint not null,
  provider                  integer,
  access_token              varchar(255),
  user_id                   bigint,
  status                    integer,
  login                     varchar(255),
  facebook_id               varchar(255),
  modified_at               timestamp not null,
  constraint ck_social_profile_provider check (provider in (0,1,2)),
  constraint ck_social_profile_status check (status in (0,1,2)),
  constraint uq_social_profile_facebook_id unique (facebook_id),
  constraint pk_social_profile primary key (id))
;

create table token (
  id                        bigint not null,
  content                   varchar(255) not null,
  user_id                   bigint,
  target                    integer,
  since                     timestamp not null,
  constraint ck_token_target check (target in (0,1)),
  constraint uq_token_content unique (content),
  constraint pk_token primary key (id))
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
  picture_id                bigint,
  modified_at               timestamp not null,
  constraint ck_users_gender check (gender in (0,1)),
  constraint ck_users_status check (status in (0,1,2,3)),
  constraint pk_users primary key (id))
;

create table user_mail_interaction (
  id                        bigint not null,
  status                    integer,
  type                      integer,
  hash                      varchar(255),
  mail                      varchar(255),
  user_id                   bigint,
  created_at                timestamp without time zone default now() not null,
  modified_at               timestamp not null,
  constraint ck_user_mail_interaction_status check (status in (0,1,2)),
  constraint ck_user_mail_interaction_type check (type in (0,1)),
  constraint pk_user_mail_interaction primary key (id))
;

create table action_wants (
  id                        bigint not null,
  action_id                 bigint,
  user_id                   bigint,
  constraint pk_action_wants primary key (id))
;

create table wishlist (
  id                        bigint not null,
  uuid                      varchar(255) not null,
  title                     varchar(255),
  description               varchar(255),
  user_id                   bigint,
  status                    integer,
  action_id                 bigint,
  modified_at               timestamp not null,
  constraint ck_wishlist_status check (status in (0,1,2)),
  constraint uq_wishlist_uuid unique (uuid),
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


create table actions_report_user (
  user_id                        bigint not null,
  action_report_id               bigint not null,
  constraint pk_actions_report_user primary key (user_id, action_report_id))
;

create table administrators_roles (
  administrators_id              bigint not null,
  roles_id                       bigint not null,
  constraint pk_administrators_roles primary key (administrators_id, roles_id))
;
create sequence actions_id_seq;

create sequence actions_report_id_seq;

create sequence action_shares_id_seq;

create sequence administrators_id_seq;

create sequence action_comments_id_seq;

create sequence friends_circle_seq;

create sequence manufacturer_id_seq;

create sequence user_mobiles_id_seq;

create sequence multimedia_id_seq;

create sequence user_notifications_id_seq;

create sequence product_id_seq;

create sequence roles_id_seq;

create sequence social_profile_id_seq;

create sequence token_id_seq;

create sequence user_id_seq;

create sequence user_mail_interaction_id_seq;

create sequence action_wants_id_seq;

create sequence wishlist_id_seq;

create sequence wishlist_product_id_seq;

alter table actions add constraint fk_actions_from_1 foreign key (from_id) references users (id);
create index ix_actions_from_1 on actions (from_id);
alter table actions add constraint fk_actions_user_2 foreign key (user_id) references users (id);
create index ix_actions_user_2 on actions (user_id);
alter table actions_report add constraint fk_actions_report_action_3 foreign key (action_id) references actions (id);
create index ix_actions_report_action_3 on actions_report (action_id);
alter table action_shares add constraint fk_action_shares_action_4 foreign key (action_id) references actions (id);
create index ix_action_shares_action_4 on action_shares (action_id);
alter table action_shares add constraint fk_action_shares_user_5 foreign key (user_id) references users (id);
create index ix_action_shares_user_5 on action_shares (user_id);
alter table action_comments add constraint fk_action_comments_action_6 foreign key (action_id) references actions (id);
create index ix_action_comments_action_6 on action_comments (action_id);
alter table action_comments add constraint fk_action_comments_user_7 foreign key (user_id) references users (id);
create index ix_action_comments_user_7 on action_comments (user_id);
alter table user_mobiles add constraint fk_user_mobiles_user_8 foreign key (user_id) references users (id);
create index ix_user_mobiles_user_8 on user_mobiles (user_id);
alter table user_mobiles add constraint fk_user_mobiles_token_9 foreign key (token_id) references token (id);
create index ix_user_mobiles_token_9 on user_mobiles (token_id);
alter table user_notifications add constraint fk_user_notifications_user_10 foreign key (user_id) references users (id);
create index ix_user_notifications_user_10 on user_notifications (user_id);
alter table user_notifications add constraint fk_user_notifications_action_11 foreign key (action_id) references actions (id);
create index ix_user_notifications_action_11 on user_notifications (action_id);
alter table product add constraint fk_product_manufacturer_12 foreign key (manufacturer_id) references manufacturer (id);
create index ix_product_manufacturer_12 on product (manufacturer_id);
alter table product add constraint fk_product_multimedia_13 foreign key (multimedia) references multimedia (id);
create index ix_product_multimedia_13 on product (multimedia);
alter table social_profile add constraint fk_social_profile_user_14 foreign key (user_id) references users (id);
create index ix_social_profile_user_14 on social_profile (user_id);
alter table token add constraint fk_token_user_15 foreign key (user_id) references users (id);
create index ix_token_user_15 on token (user_id);
alter table users add constraint fk_users_picture_16 foreign key (picture_id) references multimedia (id);
create index ix_users_picture_16 on users (picture_id);
alter table user_mail_interaction add constraint fk_user_mail_interaction_user_17 foreign key (user_id) references users (id);
create index ix_user_mail_interaction_user_17 on user_mail_interaction (user_id);
alter table action_wants add constraint fk_action_wants_action_18 foreign key (action_id) references actions (id);
create index ix_action_wants_action_18 on action_wants (action_id);
alter table action_wants add constraint fk_action_wants_user_19 foreign key (user_id) references users (id);
create index ix_action_wants_user_19 on action_wants (user_id);
alter table wishlist add constraint fk_wishlist_user_20 foreign key (user_id) references users (id);
create index ix_wishlist_user_20 on wishlist (user_id);
alter table wishlist add constraint fk_wishlist_action_21 foreign key (action_id) references actions (id);
create index ix_wishlist_action_21 on wishlist (action_id);
alter table wishlist_product add constraint fk_wishlist_product_wishList_22 foreign key (wishlist_id) references wishlist (id);
create index ix_wishlist_product_wishList_22 on wishlist_product (wishlist_id);
alter table wishlist_product add constraint fk_wishlist_product_product_23 foreign key (product_id) references product (id);
create index ix_wishlist_product_product_23 on wishlist_product (product_id);



alter table actions_report_user add constraint fk_actions_report_user_action_01 foreign key (user_id) references actions_report (id);

alter table actions_report_user add constraint fk_actions_report_user_users_02 foreign key (action_report_id) references users (id);

alter table administrators_roles add constraint fk_administrators_roles_admin_01 foreign key (administrators_id) references administrators (id);

alter table administrators_roles add constraint fk_administrators_roles_roles_02 foreign key (roles_id) references roles (id);

# --- !Downs

drop table if exists actions cascade;

drop table if exists actions_report cascade;

drop table if exists actions_report_user cascade;

drop table if exists action_shares cascade;

drop table if exists administrators cascade;

drop table if exists administrators_roles cascade;

drop table if exists action_comments cascade;

drop table if exists friends_circle cascade;

drop table if exists manufacturer cascade;

drop table if exists user_mobiles cascade;

drop table if exists multimedia cascade;

drop table if exists user_notifications cascade;

drop table if exists product cascade;

drop table if exists roles cascade;

drop table if exists social_profile cascade;

drop table if exists token cascade;

drop table if exists users cascade;

drop table if exists user_mail_interaction cascade;

drop table if exists action_wants cascade;

drop table if exists wishlist cascade;

drop table if exists wishlist_product cascade;

drop sequence if exists actions_id_seq;

drop sequence if exists actions_report_id_seq;

drop sequence if exists action_shares_id_seq;

drop sequence if exists administrators_id_seq;

drop sequence if exists action_comments_id_seq;

drop sequence if exists friends_circle_seq;

drop sequence if exists manufacturer_id_seq;

drop sequence if exists user_mobiles_id_seq;

drop sequence if exists multimedia_id_seq;

drop sequence if exists user_notifications_id_seq;

drop sequence if exists product_id_seq;

drop sequence if exists roles_id_seq;

drop sequence if exists social_profile_id_seq;

drop sequence if exists token_id_seq;

drop sequence if exists user_id_seq;

drop sequence if exists user_mail_interaction_id_seq;

drop sequence if exists action_wants_id_seq;

drop sequence if exists wishlist_id_seq;

drop sequence if exists wishlist_product_id_seq;

