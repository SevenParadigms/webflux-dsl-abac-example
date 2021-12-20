create extension if not exists "uuid-ossp";

CREATE TABLE authority
(
    id             uuid                     DEFAULT uuid_generate_v1mc() NOT NULL PRIMARY KEY,
    name           text
);
insert into authority(name) values('ROLE_ADMIN'), ('ROLE_USER');


CREATE TABLE local_user
(
    id             uuid                     DEFAULT uuid_generate_v1mc() NOT NULL PRIMARY KEY,
    login          text,
    password       text
);

insert into local_user(login, password)
values('admin', 'b9y3FIltPBbk7rrP80Tav8CTHRBRfg=='),
      ('user', 'b9y3FIltPBbk7rrP80Tav8CTHRBRfg==');

CREATE FUNCTION getAuthority(p_name text) RETURNS uuid
    LANGUAGE plpgsql AS
$$
declare
    authority_id uuid;
BEGIN
    SELECT id into authority_id FROM authority WHERE lower(name) = lower(p_name);
    RETURN authority_id;
END;
$$;

CREATE FUNCTION getUser(p_name text) RETURNS uuid
    LANGUAGE plpgsql AS
$$
declare
   user_id uuid;
BEGIN
    SELECT id into user_id FROM local_user WHERE lower(login) = lower(p_name);
    RETURN user_id;
END;
$$;

CREATE TABLE authority_user
(
    id             uuid DEFAULT uuid_generate_v1mc() NOT NULL PRIMARY KEY,
    authority_id   uuid REFERENCES authority(id) ON DELETE CASCADE,
    user_id        uuid REFERENCES local_user(id) ON DELETE CASCADE
);

insert into authority_user(authority_id, user_id)
values(getAuthority('ROLE_ADMIN'), getUser('admin')),
      (getAuthority('ROLE_USER'), getUser('admin')),
      (getAuthority('ROLE_USER'), getUser('user'));


CREATE TABLE abac_rule
(
    id             uuid DEFAULT uuid_generate_v1mc() NOT NULL PRIMARY KEY,
    name           text,
    domain_type    text,
    target         text,
    condition      text
);

insert into abac_rule(name, domain_type, target, condition)
values('Test Rule', 'Dsl', 'action == ''findAll'' and subject.roles.contains(''ROLE_ADMIN'')', 'domainObject.sort == ''id:desc'''),
      ('IP Rule', 'Dsl', 'action == ''findAll'' and environment.ip == ''192.168.2.207''', 'domainObject.sort == ''id:desc'''),
      ('Query jtree not null Rule', 'Dsl', 'action == ''findAll'' and subject.roles.contains(''ROLE_ADMIN'')', 'domainObject.query == ''!@jtree'' and domainObject.fields ==''id''  and domainObject.sort == ''id:desc'''),
      ('Query equals jsonb field Rule', 'Dsl', 'action == ''findAll'' and subject.roles.contains(''ROLE_ADMIN'')', 'domainObject.query == ''jtree.name==Acme doc'' and domainObject.sort == ''id:desc'''),
      ('Query equals jsonb field in Rule', 'Dsl', 'action == ''findAll'' and subject.roles.contains(''ROLE_ADMIN'')', 'domainObject.query == ''jtree.name^^Acme doc'' and domainObject.sort == ''id:desc''');
