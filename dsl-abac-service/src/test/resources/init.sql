create extension if not exists "uuid-ossp";
create extension if not exists rum;

CREATE TABLE form
(
    id         uuid                     DEFAULT uuid_generate_v1mc() NOT NULL PRIMARY KEY,
    jtree      jsonb                                                 NOT NULL,
    created_at timestamp with time zone DEFAULT timezone('utc'::text, CURRENT_TIMESTAMP)
);

CREATE TABLE jfolder
(
    id         uuid                     DEFAULT uuid_generate_v1mc() NOT NULL PRIMARY KEY,
    jtree      jsonb                                                 NOT NULL,
    form_id    uuid REFERENCES form (id),
    parent_id  uuid REFERENCES jfolder (id),
    created_at timestamp with time zone DEFAULT timezone('utc'::text, CURRENT_TIMESTAMP)
);

CREATE TABLE jobject
(
    id         uuid                     DEFAULT uuid_generate_v1mc() NOT NULL,
    jfolder_id uuid                                                  NOT NULL REFERENCES jfolder (id),
    jtree      jsonb                                                 NOT NULL,
    tsv        tsvector,
    created_at timestamp with time zone NULL,
    created_by uuid                     NULL,
    updated_at timestamp with time zone NULL,
    updated_by uuid                     NULL,
    PRIMARY KEY (id, jfolder_id)
) PARTITION BY LIST (jfolder_id);

CREATE TABLE jobject_jobject
(
    id        uuid DEFAULT uuid_generate_v1mc() NOT NULL PRIMARY KEY,
    left_id   uuid                              NOT NULL,
    right_id  uuid                              NOT NULL,
    jtree     jsonb,
    parent_id uuid REFERENCES jobject_jobject (id)
);

CREATE FUNCTION jfolder(p_name text) RETURNS uuid
    LANGUAGE plpgsql AS
$$
declare
    jfolder_id uuid;
BEGIN
    SELECT id into jfolder_id FROM jfolder WHERE lower(jtree->>'name') = lower(p_name);
    RETURN jfolder_id;
END;
$$;

CREATE OR REPLACE FUNCTION jtree(p_parent_id uuid) RETURNS SETOF jsonb AS
$$
BEGIN
    RETURN QUERY EXECUTE '
        select case
            when count(x) > 0 then
                jsonb_build_object(''id'', t.id, ''name'', t.jtree ->> ''name'', ''items'', jsonb_agg(f.x))
            else
                jsonb_build_object(''id'', t.id, ''name'', t.jtree ->> ''name'')
            end jtree
        from jfolder t left join jtree(t.id) as f(x) on true
        where t.parent_id = $1 or (t.parent_id is null and $1 is null)
        group by t.id, t.jtree ->> ''name'''
        USING $1;
END
$$ LANGUAGE plpgsql
   SECURITY DEFINER
   IMMUTABLE;


CREATE FUNCTION tsv_update() RETURNS trigger AS
$$
begin
    if  (TG_OP = 'UPDATE') then
        update jobject
        set tsv = setweight(to_tsvector('pg_catalog.english', new.jtree->>'name'), 'A') || setweight(to_tsvector('pg_catalog.english', new.jtree), 'B')
        where id = old.id;
        return null;
        end if;

    if  (TG_OP = 'INSERT') then
        update jobject
        set tsv = setweight(to_tsvector('pg_catalog.english', new.jtree->>'name'), 'A') || setweight(to_tsvector('pg_catalog.english', new.jtree), 'B')
        where id = new.id;
        return null;
        end if;
end
$$ LANGUAGE plpgsql;


CREATE TRIGGER update_tsv
    AFTER INSERT OR UPDATE OF jtree
    ON jobject
    FOR EACH ROW
EXECUTE FUNCTION tsv_update();


CREATE FUNCTION notify_sender() returns trigger
    LANGUAGE plpgsql
as
$$
BEGIN
    PERFORM pg_notify(
                    TG_TABLE_NAME,
                    json_build_object(
                            'operation', TG_OP,
                            'record', row_to_json(NEW)
                        )::text
                );
    RETURN NULL;
END;
$$;

CREATE trigger jobject_notify
    after insert or update OF jtree
    on jobject
    for each row
EXECUTE FUNCTION notify_sender();

INSERT INTO jfolder(jtree) VALUES ('{"name": "Document"}');
INSERT INTO jfolder(jtree) VALUES ('{"name": "Organization"}');
INSERT INTO jfolder(jtree) VALUES ('{"name": "Employee"}');
INSERT INTO jfolder(jtree) VALUES ('{"name": "File"}');
INSERT INTO jfolder(jtree) VALUES ('{"name": "Type"}');

CREATE TABLE jobject_document PARTITION OF jobject FOR VALUES IN (jfolder('document'));
CREATE TABLE jobject_organization PARTITION OF jobject FOR VALUES IN (jfolder('organization'));
CREATE TABLE jobject_employee PARTITION OF jobject FOR VALUES IN (jfolder('employee'));
CREATE TABLE jobject_file PARTITION OF jobject FOR VALUES IN (jfolder('file'));
CREATE TABLE jobject_type PARTITION OF jobject FOR VALUES IN (jfolder('type'));

INSERT INTO jfolder(parent_id, jtree) VALUES (jfolder('type'), '{"name": "email"}');
INSERT INTO jfolder(parent_id, jtree) VALUES (jfolder('type'), '{"name": "sms"}');
INSERT INTO jfolder(parent_id, jtree) VALUES (jfolder('email'), '{"name": "email_template"}');
INSERT INTO jfolder(parent_id, jtree) VALUES (jfolder('email'), '{"name": "email_draft"}');
INSERT INTO jfolder(parent_id, jtree) VALUES (jfolder('sms'), '{"name": "sms_personal"}');
INSERT INTO jfolder(parent_id, jtree) VALUES (jfolder('sms'), '{"name": "sms_distribute"}');

INSERT INTO jobject(jfolder_id, jtree) VALUES (jfolder('organization'), '{"name": "Acme", "description": "To infinity... and beyond"}'),
                                              (jfolder('organization'), '{"name": "Acme or", "description": "To infinity... and beyond"}'),
                                              (jfolder('organization'), '{"name": "Acme type", "description": "To infinity... and beyond"}'),
                                              (jfolder('document'), '{"name": "Acme doc", "description": "To infinity... and beyond"}'),
                                              (jfolder('employee'), '{"name": "Acme emp", "description": "To infinity... and beyond"}'),
                                              (jfolder('file'), '{"name": "Acme file", "description": "To infinity... and beyond"}'),
                                              (jfolder('type'), '{"name": "Acme type", "description": "To infinity... and beyond"}');

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

CREATE TABLE feature
(
    id                  uuid                     DEFAULT uuid_generate_v1mc() NOT NULL,
    "group"             text                     NULL,
    version             integer                  NULL,
    version_ann         timestamp with time zone NULL,
    custom_version      integer                  NULL,
    created_at          timestamp with time zone NULL,
    created_ann         timestamp with time zone NULL,
    custom_create       timestamp                NULL,
    custom_update       timestamp with time zone NULL,
    attr_update         timestamp                NULL,
    equality            integer                  NULL,
    custom_equality     text                     NULL,
    readonly            text                     NULL,
    custom_readonly     integer                  NULL
);