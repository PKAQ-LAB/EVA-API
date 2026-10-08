-- 账号和可选管理资料分离；保留已有 ID、密码哈希、审计列及授权关联。
-- 必须在完整备份后与新版本应用一起部署，不允许旧版本继续写 SYS_USER。
DO $$
DECLARE
    target_schema TEXT;
    target_schemas TEXT[];
    old_index TEXT;
    account_count BIGINT;
    profile_count BIGINT;
BEGIN
    target_schemas := ARRAY[current_schema()];
    -- 仅升级平台登记的租户 schema，禁止扫描或修改无关业务 schema。
    IF to_regclass(format('%I.sys_tenant', current_schema())) IS NOT NULL THEN
        SELECT target_schemas || COALESCE(array_agg(DISTINCT SCHEMA_NAME), ARRAY[]::TEXT[])
        INTO target_schemas FROM SYS_TENANT
        WHERE SCHEMA_NAME IS NOT NULL AND SCHEMA_NAME <> current_schema()
          AND to_regnamespace(SCHEMA_NAME) IS NOT NULL;
    END IF;

    FOREACH target_schema IN ARRAY target_schemas LOOP
        IF target_schema !~ '^[a-z][a-z0-9_]{0,62}$' THEN
            RAISE EXCEPTION 'invalid account schema';
        END IF;
        IF to_regclass(format('%I.sys_user', target_schema)) IS NOT NULL THEN
            IF to_regclass(format('%I.sys_account', target_schema)) IS NOT NULL
                OR to_regclass(format('%I.sys_account_profile', target_schema)) IS NOT NULL THEN
                RAISE EXCEPTION 'account migration found overlapping old and new tables in %', target_schema;
            END IF;
            EXECUTE format('ALTER TABLE %I.sys_user RENAME TO sys_account', target_schema);
            EXECUTE format(
                'CREATE TABLE %I.sys_account_profile (
                    ACCOUNT_ID BIGINT PRIMARY KEY REFERENCES %I.sys_account(ID),
                    CODE VARCHAR(100), NAME VARCHAR(100), DEPT_ID BIGINT)',
                target_schema, target_schema);
            EXECUTE format(
                'INSERT INTO %I.sys_account_profile(ACCOUNT_ID, CODE, NAME, DEPT_ID)
                 SELECT ID, CODE, NAME, DEPT_ID FROM %I.sys_account',
                target_schema, target_schema);
            -- 只有资料复制完整后才能移除账号表上的管理字段。
            EXECUTE format('SELECT COUNT(*) FROM %I.sys_account', target_schema) INTO account_count;
            EXECUTE format('SELECT COUNT(*) FROM %I.sys_account_profile', target_schema) INTO profile_count;
            IF account_count <> profile_count THEN
                RAISE EXCEPTION 'account profile migration count mismatch in %', target_schema;
            END IF;
            EXECUTE format(
                'ALTER TABLE %I.sys_account DROP COLUMN CODE, DROP COLUMN NAME, DROP COLUMN DEPT_ID',
                target_schema);
            -- 原账号唯一索引及外键随表重命名保留，不重新分配账号 ID。
            FOR old_index IN
                SELECT indexname FROM pg_indexes
                WHERE schemaname = target_schema AND tablename = 'sys_account'
                  AND indexname LIKE '%sys_user%'
            LOOP
                EXECUTE format('ALTER INDEX %I.%I RENAME TO %I', target_schema, old_index,
                               replace(old_index, 'sys_user', 'sys_account'));
            END LOOP;
            EXECUTE format(
                'CREATE INDEX IDX_SYS_ACCOUNT_PROFILE_DEPT_ID ON %I.sys_account_profile(DEPT_ID)',
                target_schema);
            EXECUTE format(
                'CREATE INDEX IDX_SYS_ACCOUNT_PROFILE_CODE ON %I.sys_account_profile(CODE)',
                target_schema);
        ELSIF to_regclass(format('%I.sys_account', target_schema)) IS NULL
            OR to_regclass(format('%I.sys_account_profile', target_schema)) IS NULL THEN
            RAISE EXCEPTION 'account migration missing source tables in %', target_schema;
        END IF;
        IF to_regclass(format('%I.eva_tenant_schema_version', target_schema)) IS NOT NULL THEN
            EXECUTE format(
                'INSERT INTO %I.eva_tenant_schema_version(VERSION) VALUES (''2'')
                 ON CONFLICT (VERSION) DO NOTHING', target_schema);
        END IF;
    END LOOP;
END;
$$;
