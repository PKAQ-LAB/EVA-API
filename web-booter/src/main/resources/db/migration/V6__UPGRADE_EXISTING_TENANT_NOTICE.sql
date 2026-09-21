DO $$
DECLARE
    tenant_record RECORD;
BEGIN
    FOR tenant_record IN
        SELECT DISTINCT SCHEMA_NAME
        FROM SYS_TENANT
        WHERE COALESCE(DELETED, 0) = 0
          AND SCHEMA_NAME IS NOT NULL
          AND SCHEMA_NAME <> ''
    LOOP
        IF tenant_record.SCHEMA_NAME !~ '^[a-z][a-z0-9_]{0,62}$' THEN
            RAISE EXCEPTION '非法租户 schema 名称: %', tenant_record.SCHEMA_NAME;
        END IF;
        IF to_regnamespace(tenant_record.SCHEMA_NAME) IS NULL THEN
            RAISE EXCEPTION '租户 schema 不存在: %', tenant_record.SCHEMA_NAME;
        END IF;

        EXECUTE format($sql$
            CREATE TABLE IF NOT EXISTS %I.SYS_NOTICE (
                ID BIGINT PRIMARY KEY,
                REVISION INTEGER,
                DELETED BIGINT NOT NULL DEFAULT 0,
                FROZEN INTEGER DEFAULT 0,
                SORT DOUBLE PRECISION NOT NULL DEFAULT 0,
                CREATE_ID BIGINT,
                CREATE_BY VARCHAR(100),
                UTC_CREATE TIMESTAMP,
                MODIFY_ID BIGINT,
                MODIFY_BY VARCHAR(100),
                UTC_MODIFY TIMESTAMP,
                REMARK VARCHAR(400),
                TITLE VARCHAR(200) NOT NULL,
                CONTENT TEXT,
                TYPE VARCHAR(40) NOT NULL,
                AVATAR VARCHAR(500),
                DATETIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
        $sql$, tenant_record.SCHEMA_NAME);
        EXECUTE format('CREATE INDEX IF NOT EXISTS IDX_SYS_NOTICE_TYPE ON %I.SYS_NOTICE(TYPE)',
                tenant_record.SCHEMA_NAME);
        EXECUTE format('CREATE INDEX IF NOT EXISTS IDX_SYS_NOTICE_DATETIME ON %I.SYS_NOTICE(DATETIME DESC)',
                tenant_record.SCHEMA_NAME);
    END LOOP;
END $$;
