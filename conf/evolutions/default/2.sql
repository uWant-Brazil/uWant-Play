# --- !Ups
insert into multimedia (id,file_name,url,cdn,modified_at) values (nextVal('multimedia_id_seq'),'zeus.jpg','http://static.comicvine.com/uploads/original/8/83062/1539974-zeus_greek_mythology_687267_1024_768.jpg',0,now());
insert into multimedia (id,file_name,url,cdn,modified_at) values (nextVal('multimedia_id_seq'),'poseidon.jpg','http://fc04.deviantart.net/fs71/f/2013/317/d/d/pjato__poseidon_by_daekazu-d6u4xzc.jpg',0,now());

insert into users (id,login,password,mail,name,birthday,since,gender,status,modified_at,picture_id) values (nextVal('user_id_seq'),'zeus',md5('0limp0'),'zeus@olimpo.god.com','Zeus Cronos da Silva','1000-09-01 00:00:00.00',now(),0,0,now(),(select id from multimedia where file_name = 'zeus.jpg'));
insert into social_profile (id,provider,access_token,user_id,status,modified_at,login) values (nextVal('social_profile_id_seq'),1,'access_token',1,0,now(),'zeus-apx@facebook.com');
insert into user_mail_interaction (id,status,type,hash,mail,user_id,modified_at) values (nextVal('user_mail_interaction_id_seq'),1,1,'HAHAHA','zeus@olimpo.god.com',1,now());
insert into user_mail_interaction (id,status,type,hash,mail,user_id,modified_at) values (nextVal('user_mail_interaction_id_seq'),0,0,'HAHAHA','zeus@olimpo.god.com',1,now());

insert into token (id,content,user_id,since) values (nextVal('token_id_seq'),'UNIQUE_CONTENT_TOKEN',1,now());
insert into user_mobiles (id,identifier,user_id,token_id,os,modified_at) values (nextVal('user_mobiles_id_seq'),'adSKkkanem21j23ldjjdkdas-deviceId',1,1,0,now());

insert into actions (id,type,from_id,user_id,extra,created_at,modified_at) values (nextVal('actions_id_seq'),8,(select id from users where login = 'zeus'),(select id from users where login = 'zeus'),'#IMORAL #SENSACIONAL',now(),now());

insert into manufacturer (id,name,modified_at) values (nextVal('manufacturer_id_seq'),'uWant',now());

insert into wishlist (id,title,description,user_id,status,modified_at,action_id,uuid) values (nextVal('wishlist_id_seq'),'Desejos do Olimpo','O que todo DEUS dejesa!',1,0,now(),(select id from actions where type=8 and extra = '#IMORAL #SENSACIONAL'),'8a2d2c80-3ab7-11e4-ba70-0002a5d5c51b' );

insert into multimedia (id,file_name,url,cdn,modified_at) values (nextVal('multimedia_id_seq'),'play4.jpg','http://mlb-s1-p.mlstatic.com/playstation-4-sony-500gb-bivolt-ps4-bluray-play4-17346-MLB20137063623_072014-F.jpg',0,now());
insert into product (id,name,nick_name,modified_at,multimedia,manufacturer_id) values (nextVal('product_id_seq'),'Sony Playstation 4','Play 4',now(),(select id from multimedia where file_name = 'play4.jpg'),(select id from manufacturer where name = 'uWant'));
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,1,0);

insert into multimedia (id,file_name,url,cdn,modified_at) values (nextVal('multimedia_id_seq'),'treloso.jpg','http://portalneuronio.ne10.uol.com.br/imagens/noticia/normal/4e3c43a255be8.jpg',0,now());
insert into product (id,name,modified_at,multimedia,manufacturer_id) values (nextVal('product_id_seq'),'Biscoito Treloso',now(),(select id from multimedia where file_name = 'treloso.jpg'),(select id from manufacturer where name = 'uWant'));
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,2,2);

insert into multimedia (id,file_name,url,cdn,modified_at) values (nextVal('multimedia_id_seq'),'cotonete.jpg','http://1.bp.blogspot.com/-ZKnfubbI_rs/UTduXWtHjZI/AAAAAAAAApg/7nqfrUTiTKk/s1600/Cotonete.gif',0,now());
insert into product (id,name,nick_name,modified_at,multimedia,manufacturer_id) values (nextVal('product_id_seq'),'Astes flexíveis','Cotonete',now(),(select id from multimedia where file_name = 'cotonete.jpg'),(select id from manufacturer where name = 'uWant'));
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,3,0);

insert into multimedia (id,file_name,url,cdn,modified_at) values (nextVal('multimedia_id_seq'),'smarttv.jpg','http://www.samsung.com/us/microsite/2012-smart-tv-bb/img/intro-tvSlide1.png',0,now());
insert into product (id,name,modified_at,multimedia,manufacturer_id) values (nextVal('product_id_seq'),'SmartTV',now(),(select id from multimedia where file_name = 'smarttv.jpg'),(select id from manufacturer where name = 'uWant'));
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,4,0);

insert into multimedia (id,file_name,url,cdn,modified_at) values (nextVal('multimedia_id_seq'),'tabajara.jpg','http://sidhugo.zip.net/images/tabajara.jpg',0,now());
insert into product (id,name,modified_at,multimedia,manufacturer_id) values (nextVal('product_id_seq'),'Camisa do Tabajara FC',now(),(select id from multimedia where file_name = 'tabajara.jpg'),(select id from manufacturer where name = 'uWant'));
insert into wishlist_product (id,wishlist_id,product_id,status) values (nextVal('wishlist_product_id_seq'),1,5,0);

# --- !Downs
