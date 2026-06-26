-- 组织 / 部门管理（sys_organization）树形结构规范化
-- 与 sys_module / sys_post 范本对齐
-- PostgreSQL 兼容

-- ====================================================================
-- 1. pid 类型规范化：VARCHAR → BIGINT, NOT NULL DEFAULT 0
-- ====================================================================

ALTER TABLE sys_organization
    ALTER COLUMN pid TYPE BIGINT USING (
        CASE
            WHEN pid IS NULL OR pid = '' OR pid = '0' THEN 0
            ELSE CAST(pid AS BIGINT)
        END
    );
ALTER TABLE sys_organization ALTER COLUMN pid SET DEFAULT 0;
ALTER TABLE sys_organization ALTER COLUMN pid SET NOT NULL;

COMMENT ON COLUMN sys_organization.pid IS '上级节点ID，根节点 = 0';

-- ====================================================================
-- 2. isleaf 类型规范化：TINYINT/VARCHAR → BOOLEAN
-- ====================================================================

ALTER TABLE sys_organization
    ALTER COLUMN isleaf TYPE BOOLEAN USING (
        CASE
            WHEN isleaf IS NULL THEN TRUE
            WHEN isleaf::text IN ('true', '1', 't', 'T', 'y', 'Y') THEN TRUE
            ELSE FALSE
        END
    );

COMMENT ON COLUMN sys_organization.isleaf IS '是否叶子节点';

-- ====================================================================
-- 3. 修复存量 path 为空 / 格式异常的节点（递归 CTE）
-- ====================================================================

WITH RECURSIVE node_tree AS (
    SELECT id, pid, ('/' || id::text)::varchar AS new_path
    FROM sys_organization
    WHERE pid = 0

    UNION ALL

    SELECT c.id, c.pid, (t.new_path || '/' || c.id::text)::varchar
    FROM sys_organization c
    JOIN node_tree t ON c.pid = t.id
)
UPDATE sys_organization m
SET path = t.new_path
FROM node_tree t
WHERE m.id = t.id
  AND (m.path IS NULL OR m.path = '' OR m.path NOT LIKE '/%');

COMMENT ON COLUMN sys_organization.path IS '路径（id 链），根节点 = /id，子节点 = /父path/id';

-- ====================================================================
-- 4. 索引
-- ====================================================================

CREATE INDEX IF NOT EXISTS idx_sys_organization_pid ON sys_organization(pid);
CREATE INDEX IF NOT EXISTS idx_sys_organization_path ON sys_organization(path);
