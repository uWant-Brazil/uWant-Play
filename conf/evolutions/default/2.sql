# --- !Ups
insert into multimedia (id,file_name,url,cdn,modified_at) values (nextVal('multimedia_id_seq'),'zeus.jpg','http://static.comicvine.com/uploads/original/8/83062/1539974-zeus_greek_mythology_687267_1024_768.jpg',0,now());
insert into multimedia (id,file_name,url,cdn,modified_at) values (nextVal('multimedia_id_seq'),'poseidon.jpg','http://fc04.deviantart.net/fs71/f/2013/317/d/d/pjato__poseidon_by_daekazu-d6u4xzc.jpg',0,now());

insert into users (id,login,password,mail,name,birthday,since,gender,status,modified_at,picture_id) values (nextVal('user_id_seq'),'zeus',md5('0limp0'),'zeus@olimpo.god.com','Zeus Cronos da Silva','1000-09-01 00:00:00.00',now(),0,0,now(),(select id from multimedia where file_name = 'zeus.jpg'));
insert into social_profile (id,provider,access_token,user_id,status,modified_at,login) values (nextVal('social_profile_id_seq'),1,'access_token',1,0,now(),'zeus-apx@facebook.com');
insert into user_mail_interaction (id,status,type,hash,mail,user_id,modified_at) values (nextVal('user_mail_interaction_id_seq'),1,1,'HAHAHA','zeus@olimpo.god.com',1,now());
insert into user_mail_interaction (id,status,type,hash,mail,user_id,modified_at) values (nextVal('user_mail_interaction_id_seq'),0,0,'HAHAHA','zeus@olimpo.god.com',1,now());
# --- !Downs
