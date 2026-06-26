-- 岗位管理（sys_post）树形结构规范化 + 岗位-用户关系表（sys_postuser_ref）字段补全
-- 与 sys_module 范本对齐
-- PostgreSQL 兼容

-- ====================================================================
-- 1. sys_post 字段类型升级
-- ====================================================================

-- 1.1 pid: VARCHAR → BIGINT, NOT NULL DEFAULT 0
ALTER TABLE sys_post
    ALTER COLUMN pid TYPE BIGINT USING (
        CASE
            WHEN pid IS NULL OR pid = '' THEN 0
            ELSE CAST(pid AS BIGINT)
        END
    );
ALTER TABLE sys_post ALTER COLUMN pid SET DEFAULT 0;
ALTER TABLE sys_post ALTER COLUMN pid SET NOT NULL;

COMMENT ON COLUMN sys_post.pid IS '上级岗位ID，根节点 = 0';

-- 1.2 path_id → path（与 sys_module 命名统一）
ALTER TABLE sys_post RENAME COLUMN path_id TO path;
COMMENT ON COLUMN sys_post.path IS '路径（id 链），根节点 = /id，子节点 = /父path/id';

-- 1.3 is_leaf: VARCHAR → BOOLEAN
ALTER TABLE sys_post
    ALTER COLUMN is_leaf TYPE BOOLEAN USING (
        CASE
            WHEN is_leaf IS NULL OR is_leaf = '' THEN TRUE
            WHEN LOWER(is_leaf) IN ('false', '0', 'f', 'n', 'no') THEN FALSE
            ELSE TRUE
        END
    );
COMMENT ON COLUMN sys_post.is_leaf IS '是否叶子节点';

-- ====================================================================
-- 2. 修复存量 path 为空 / 格式异常的节点（递归 CTE）
-- ====================================================================

WITH RECURSIVE node_tree AS (
    -- 根节点
    SELECT id, pid, ('/' || id::text)::varchar AS new_path
    FROM sys_post
    WHERE pid = 0

    UNION ALL

    -- 子节点：拼上父节点的 new_path
    SELECT c.id, c.pid, (t.new_path || '/' || c.id::text)::varchar
    FROM sys_post c
    JOIN node_tree t ON c.pid = t.id
)
UPDATE sys_post m
SET path = t.new_path
FROM node_tree t
WHERE m.id = t.id
  AND (m.path IS NULL OR m.path = '' OR m.path NOT LIKE '/%');

-- ====================================================================
-- 3. sys_post 索引
-- ====================================================================

CREATE INDEX IF NOT EXISTS idx_sys_post_pid ON sys_post(pid);
CREATE INDEX IF NOT EXISTS idx_sys_post_path ON sys_post(path);

-- ====================================================================
-- 4. sys_postuser_ref 关系表强化
-- ====================================================================

-- 4.1 补独立主键 + 租户字段 + 审计字段
ALTER TABLE sys_postuser_ref ADD COLUMN IF NOT EXISTS id BIGINT;
ALTER TABLE sys_postuser_ref ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
ALTER TABLE sys_postuser_ref ADD COLUMN IF NOT EXISTS create_id BIGINT;
ALTER TABLE sys_postuser_ref ADD COLUMN IF NOT EXISTS create_by VARCHAR(64);
ALTER TABLE sys_postuser_ref ADD COLUMN IF NOT EXISTS utc_create TIMESTAMP;

-- 4.2 历史数据补 id（基于 epoch 微秒 + random，确保唯一）
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN SELECT ctid FROM sys_postuser_ref WHERE id IS NULL LOOP
        UPDATE sys_postuser_ref
        SET id = (EXTRACT(EPOCH FROM CLOCK_TIMESTAMP()) * 1000000)::BIGINT
                  + (RANDOM() * 1000000)::BIGINT
        WHERE ctid = r.ctid;
    END LOOP;
END $$;

-- 4.3 id 设为主键
ALTER TABLE sys_postuser_ref ALTER COLUMN id SET NOT NULL;
ALTER TABLE sys_postuser_ref ADD PRIMARY KEY (id);

-- 4.4 联合唯一约束：防止重复授权
ALTER TABLE sys_postuser_ref ADD CONSTRAINT uk_postuser_post_user UNIQUE (post_id, user_id);

-- 4.5 反向查询索引
CREATE INDEX IF NOT EXISTS idx_sys_postuser_user_id ON sys_postuser_ref(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_postuser_post_id ON sys_postuser_ref(post_id);

-- 4.6 字段注释
COMMENT ON COLUMN sys_postuser_ref.id IS '主键';
COMMENT ON COLUMN sys_postuser_ref.post_id IS '岗位ID';
COMMENT ON COLUMN sys_postuser_ref.user_id IS '用户ID';
COMMENT ON COLUMN sys_postuser_ref.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_postuser_ref.create_id IS '授权人ID';
COMMENT ON COLUMN sys_postuser_ref.create_by IS '授权人';
COMMENT ON COLUMN sys_postuser_ref.utc_create IS '授权时间';
