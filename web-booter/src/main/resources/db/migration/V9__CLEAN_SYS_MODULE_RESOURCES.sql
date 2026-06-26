-- 清理模块资源子表的历史 batchId 标记字段，并补齐租户字段与常用索引。
-- R1 说明：
-- 1. 资源保存已改为 diff 模式，不再使用 batch_id。
-- 2. StdLineEntity.tenantId 已配置 INSERT 自动填充，新数据会自动写入 tenant_id。

ALTER TABLE sys_module_resources
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

ALTER TABLE sys_module_resources
    DROP COLUMN IF EXISTS batch_id;

CREATE INDEX IF NOT EXISTS idx_sys_module_resources_main_id
    ON sys_module_resources(main_id);

CREATE INDEX IF NOT EXISTS idx_sys_module_resources_tenant_id
    ON sys_module_resources(tenant_id);

COMMENT ON COLUMN sys_module_resources.tenant_id IS '租户ID';
