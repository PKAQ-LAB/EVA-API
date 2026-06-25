-- 模块管理（sys_module）树形结构规范化
-- 1) pid 统一为 NOT NULL DEFAULT 0，根节点用 0 作哨兵
-- 2) path 统一为 /id 链格式，修复存量 path 为空/格式异常的节点
-- 3) 补 pid / path 索引

-- ----------------------------------------------------------------------
-- 1. pid 规范化
-- ----------------------------------------------------------------------

-- 1.1 存量 NULL 刷成 0
UPDATE sys_module SET pid = 0 WHERE pid IS NULL;

-- 1.2 设默认值 + 非空约束
ALTER TABLE sys_module ALTER COLUMN pid SET DEFAULT 0;
ALTER TABLE sys_module ALTER COLUMN pid SET NOT NULL;

COMMENT ON COLUMN sys_module.pid IS '父节点ID，根节点 = 0';

-- ----------------------------------------------------------------------
-- 2. path 规范化（仅修复 NULL/空/不以 / 开头的脏数据，已正确的不动）
-- ----------------------------------------------------------------------

WITH RECURSIVE node_tree AS (
    -- 根节点
    SELECT id, pid, ('/' || id::text)::varchar AS new_path
    FROM sys_module
    WHERE pid = 0

    UNION ALL

    -- 子节点：拼上父节点的 new_path
    SELECT c.id, c.pid, (t.new_path || '/' || c.id::text)::varchar
    FROM sys_module c
    JOIN node_tree t ON c.pid = t.id
)
UPDATE sys_module m
SET path = t.new_path
FROM node_tree t
WHERE m.id = t.id
  AND (m.path IS NULL OR m.path = '' OR m.path NOT LIKE '/%');

COMMENT ON COLUMN sys_module.path IS '路径（id 链），根节点 = /id，子节点 = /父path/id';

-- ----------------------------------------------------------------------
-- 3. 索引
-- ----------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_sys_module_pid ON sys_module(pid);
CREATE INDEX IF NOT EXISTS idx_sys_module_path ON sys_module(path);
