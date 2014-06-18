# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups
insert into users (id,login,password,mail,name,birthday,since,gender,status,modified_at) values (nextVal('user_id_seq'),'zeus','0limp0','zeus@olimpo.god.com','Zeus Cronos da Silva','1000-09-01 00:00:00.00',now(),0,0,now());
insert into social_profile (id,provider,access_token,user_id,status,modified_at) values (nextVal('social_profile_id_seq'),1,'access_token',1,0,now());
insert into social_profile_logins (id,login,profile_id) values (nextVal('social_profile_logins_id_seq'),'zeus-apx@facebook.com',1);
insert into user_mail_interaction (id,status,type,hash,mail,user_id,modified_at) values (nextVal('user_mail_interaction_id_seq'),1,1,'HAHAHA','zeus@olimpo.god.com',1,now());
insert into user_mail_interaction (id,status,type,hash,mail,user_id,modified_at) values (nextVal('user_mail_interaction_id_seq'),0,0,'HAHAHA','zeus@olimpo.god.com',1,now());

insert into token (id,content,user_id,since) values (nextVal('token_id_seq'),'UNIQUE_CONTENT_TOKEN',1,now());
insert into user_mobiles (id,identifier,user_id,token_id,modified_at) values (nextVal('user_mobiles_id_seq'),'adSKkkanem21j23ldjjdkdas-deviceId',1,1,now());

insert into wishlist (id,title,description,user_id,status,modified_at) values (nextVal('wishlist_id_seq'),'Desejos do Olimpo','O que todo DEUS dejesa!',1,0,now());

insert into product (id,name,nick_name,modified_at) values (nextVal('product_id_seq'),'Sony Playstation 4','Play 4',now());
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,1,0);

insert into product (id,name,modified_at) values (nextVal('product_id_seq'),'Biscoito Treloso',now());
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,2,2);

insert into product (id,name,nick_name,modified_at) values (nextVal('product_id_seq'),'Astes flexíveis','Cotonete',now());
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,3,0);

insert into product (id,name,modified_at) values (nextVal('product_id_seq'),'SmartTV',now());
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,4,0);

insert into product (id,name,modified_at) values (nextVal('product_id_seq'),'Camisa do Tabajara FC',now());
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,5,0);

# --- !Downs
delete from wishlist_product;
delete from wishlist;
delete from product;

delete from social_profile_logins;
delete from social_profile;
delete from user_mail_interaction;
delete from token;
delete from users;
