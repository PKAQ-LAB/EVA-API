-- 租户 schema 从“前缀 + ID”迁移为“前缀 + 不可变 code”。
-- 迁移只接受能够从既有映射精确证明的前缀，禁止猜测或清洗租户 code。
LOCK TABLE SYS_TENANT IN ACCESS EXCLUSIVE MODE;
--#

DO $$
DECLARE
    V_INVALID_COUNT BIGINT;
    V_TENANT RECORD;
    V_TARGET_SCHEMA TEXT;
BEGIN
    SELECT COUNT(*) INTO V_INVALID_COUNT
    FROM SYS_TENANT
    WHERE (ID = 0 AND (CODE IS DISTINCT FROM 'eva' OR SCHEMA_NAME IS NOT NULL))
       OR (ID > 0 AND (CODE IS NULL OR CODE !~ '^[a-z][a-z0-9_]{0,5}$'))
       OR ID < 0;
    IF V_INVALID_COUNT <> 0 THEN
        RAISE EXCEPTION 'tenant code/schema metadata violates the canonical contract';
    END IF;

    SELECT COUNT(*) - COUNT(DISTINCT CODE) INTO V_INVALID_COUNT
    FROM SYS_TENANT;
    IF V_INVALID_COUNT <> 0 THEN
        RAISE EXCEPTION 'tenant code must be globally unique, including soft-deleted tenants';
    END IF;

    SELECT COUNT(*) - COUNT(DISTINCT SCHEMA_NAME) INTO V_INVALID_COUNT
    FROM SYS_TENANT
    WHERE ID > 0;
    IF V_INVALID_COUNT <> 0 THEN
        RAISE EXCEPTION 'tenant schema mapping must be globally unique';
    END IF;

    SELECT COUNT(*) INTO V_INVALID_COUNT
    FROM SYS_TENANT
    WHERE ID > 0 AND SCHEMA_NAME IS NULL;
    IF V_INVALID_COUNT <> 0 THEN
        RAISE EXCEPTION 'tenant schema mapping is missing';
    END IF;

    SELECT COUNT(*) INTO V_INVALID_COUNT
    FROM SYS_TENANT
    WHERE ID > 0
      AND SCHEMA_NAME <> 'tenant_' || ID::TEXT
      AND SCHEMA_NAME <> 'tenant_' || CODE;
    IF V_INVALID_COUNT <> 0 THEN
        RAISE EXCEPTION 'tenant schema mapping is neither tenant_<id> nor tenant_<code>';
    END IF;

    FOR V_TENANT IN
        SELECT ID, CODE, SCHEMA_NAME
        FROM SYS_TENANT
        WHERE ID > 0
        ORDER BY ID
    LOOP
        V_TARGET_SCHEMA := 'tenant_' || V_TENANT.CODE;
        IF V_TARGET_SCHEMA !~ '^[a-z][a-z0-9_]{0,62}$'
                OR OCTET_LENGTH(V_TARGET_SCHEMA) > 63 THEN
            RAISE EXCEPTION 'target tenant schema name is invalid for tenant %', V_TENANT.ID;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM PG_NAMESPACE WHERE NSPNAME = V_TENANT.SCHEMA_NAME) THEN
            RAISE EXCEPTION 'source tenant schema is missing for tenant %', V_TENANT.ID;
        END IF;

        IF V_TENANT.SCHEMA_NAME <> V_TARGET_SCHEMA THEN
            IF V_TENANT.SCHEMA_NAME <> 'tenant_' || V_TENANT.ID::TEXT THEN
                RAISE EXCEPTION 'tenant schema mapping is neither legacy nor canonical for tenant %', V_TENANT.ID;
            END IF;
            IF EXISTS (SELECT 1 FROM PG_NAMESPACE WHERE NSPNAME = V_TARGET_SCHEMA) THEN
                RAISE EXCEPTION 'target tenant schema already exists for tenant %', V_TENANT.ID;
            END IF;

            EXECUTE FORMAT('ALTER SCHEMA %I RENAME TO %I',
                           V_TENANT.SCHEMA_NAME, V_TARGET_SCHEMA);
            UPDATE SYS_TENANT
            SET SCHEMA_NAME = V_TARGET_SCHEMA
            WHERE ID = V_TENANT.ID AND SCHEMA_NAME = V_TENANT.SCHEMA_NAME;
            IF NOT FOUND THEN
                RAISE EXCEPTION 'tenant schema mapping changed concurrently for tenant %', V_TENANT.ID;
            END IF;
        END IF;
    END LOOP;
END;
$$;
--#

DROP INDEX IF EXISTS UK_SYS_TENANT_CODE;
--#
CREATE UNIQUE INDEX UK_SYS_TENANT_CODE ON SYS_TENANT(CODE);
--#

DROP INDEX IF EXISTS UK_SYS_TENANT_SCHEMA_NAME;
--#
CREATE UNIQUE INDEX UK_SYS_TENANT_SCHEMA_NAME
    ON SYS_TENANT(SCHEMA_NAME)
    WHERE SCHEMA_NAME IS NOT NULL;
--#

ALTER TABLE SYS_TENANT ALTER COLUMN CODE SET NOT NULL;
--#

ALTER TABLE SYS_TENANT DROP CONSTRAINT IF EXISTS CK_SYS_TENANT_CODE_CANONICAL;
--#
ALTER TABLE SYS_TENANT ADD CONSTRAINT CK_SYS_TENANT_CODE_CANONICAL
    CHECK (COALESCE(
        (ID = 0 AND CODE = 'eva' AND SCHEMA_NAME IS NULL)
        OR (ID > 0 AND CODE ~ '^[a-z][a-z0-9_]{0,5}$'),
        FALSE));
--#

-- 新建 schema 必须严格失败于同名对象，禁止复用已删除租户遗留数据。
CREATE OR REPLACE FUNCTION EVA_PROVISION_TENANT_SCHEMA(P_SCHEMA_NAME TEXT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    IF P_SCHEMA_NAME IS NULL OR P_SCHEMA_NAME !~ '^[a-z][a-z0-9_]{0,62}$'
            OR OCTET_LENGTH(P_SCHEMA_NAME) > 63 THEN
        RAISE EXCEPTION 'invalid tenant schema name';
    END IF;
    EXECUTE FORMAT('CREATE SCHEMA %I', P_SCHEMA_NAME);
END;
$$;
--#
