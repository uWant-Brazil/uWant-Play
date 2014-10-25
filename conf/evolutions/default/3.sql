# --- !Ups
insert into users (id,login,password,mail,name,birthday,since,gender,status,modified_at,picture_id) values (nextVal('user_id_seq'),'poseidon',md5('0cean0'),'poseidon@olimpo.god.com','Poseidon Cronos da Silva','1000-10-01 00:00:00.00',now(),0,0,now(),(select id from multimedia where file_name = 'poseidon.jpg'));
insert into social_profile (id,provider,access_token,user_id,status,modified_at,login) values (nextVal('social_profile_id_seq'),1,'access_token_2',2,0,now(),'poseidon-band1da0@facebook.com');
insert into user_mail_interaction (id,status,type,hash,mail,user_id,modified_at) values (nextVal('user_mail_interaction_id_seq'),1,1,'HAHAHA','poseidon@olimpo.god.com',2,now());
insert into user_mail_interaction (id,status,type,hash,mail,user_id,modified_at) values (nextVal('user_mail_interaction_id_seq'),0,0,'HAHAHA','poseidon@olimpo.god.com',2,now());

insert into token (id,content,user_id,since) values (nextVal('token_id_seq'),'TOKEN_2',2,now());
insert into user_mobiles (id,identifier,user_id,token_id,os,modified_at) values (nextVal('user_mobiles_id_seq'),'haaadouuk3n-deviceId',2,2,1,now());

insert into friends_circle (requester_id, target_id) values ((select id from users where login = 'zeus'), (select id from users where login = 'poseidon'));
insert into friends_circle (requester_id, target_id) values ((select id from users where login = 'poseidon'), (select id from users where login = 'zeus'));

insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,0,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,1,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,2,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,3,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,3,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,1,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,2,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,1,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,2,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),1,2,3,null,now(),now());

insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),2,1,1,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),2,1,2,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),2,1,3,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),2,1,2,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),2,1,3,null,now(),now());
insert into actions (id,user_id,from_id,type,extra,created_at,modified_at) values (nextVal('actions_id_seq'),2,1,1,null,now(),now());

# --- !Downs