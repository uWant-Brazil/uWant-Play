# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups
insert into roles (id,name) values (nextVal('roles_id_seq'), 'role.god');
insert into administrators (id, login, name, mail, status) values (nextVal('administrators_id_seq'), 'adm.zeus', 'Zeus', 'zeus@uwant.com.br', 0);
insert into administrators_roles (administrators_id, roles_id) values ((select id from administrators where login = 'adm.zeus'), (select id from roles where name = 'role.god'))
# --- !Downs