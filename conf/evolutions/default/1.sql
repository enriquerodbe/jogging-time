# --- !Ups

create table "users" (
  "id" bigint generated by default as identity(start with 1) not null primary key,
  "email" varchar not null,
  "firstName" varchar not null,
  "lastName" varchar not null,
  "roles" varchar not null
);

create table "passwords" (
    "userEmail" varchar not null primary key,
    "hasher" text not null,
    "hash" text not null,
    "salt" text
);

insert into "users" ("email", "firstName", "lastName", "roles") values ( 'admin@jogging.com', 'admin', 'admin', 'ADMIN' );
insert into "passwords" values ( 'admin@jogging.com', 'bcrypt', '$2a$10$pjnp9/GDJxEv0tGYZRHpT.esEkQsfsq/z2TY3YpnoVpsOvzT4TE3m', null );

# --- !Downs

drop table "users" if exists;
drop table "passwords" if exists;
