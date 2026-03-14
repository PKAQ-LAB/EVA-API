-- 补全 log_biz 表缺失字段并添加租户隔离支持
ALTER TABLE log_biz ADD COLUMN IF NOT EXISTS spend_time   VARCHAR(20)  DEFAULT NULL;
ALTER TABLE log_biz ADD COLUMN IF NOT EXISTS m_code       VARCHAR(50)  DEFAULT NULL;
ALTER TABLE log_biz ADD COLUMN IF NOT EXISTS b_id         VARCHAR(40)  DEFAULT NULL;
ALTER TABLE log_biz ADD COLUMN IF NOT EXISTS post_id      BIGINT       DEFAULT NULL;
ALTER TABLE log_biz ADD COLUMN IF NOT EXISTS org_id       BIGINT       DEFAULT NULL;
ALTER TABLE log_biz ADD COLUMN IF NOT EXISTS create_id    BIGINT       DEFAULT NULL;
ALTER TABLE log_biz ADD COLUMN IF NOT EXISTS tenant_id    BIGINT       DEFAULT NULL;

COMMENT ON COLUMN log_biz.spend_time  IS '请求耗时';
COMMENT ON COLUMN log_biz.m_code      IS '模块编码';
COMMENT ON COLUMN log_biz.b_id        IS '业务数据ID';
COMMENT ON COLUMN log_biz.create_id   IS '创建人ID';
COMMENT ON COLUMN log_biz.tenant_id   IS '租户ID';

-- 租户隔离索引
CREATE INDEX IF NOT EXISTS IDX_LOG_BIZ_TENANT_ID ON log_biz(tenant_id);
-- 操作时间索引（排序 + 范围查询）
CREATE INDEX IF NOT EXISTS IDX_LOG_BIZ_OPERATE_DATETIME ON log_biz(operate_datetime);
-- 租户 + 操作时间联合索引（分页列表查询最优路径）
CREATE INDEX IF NOT EXISTS IDX_LOG_BIZ_TENANT_DATETIME ON log_biz(tenant_id, operate_datetime);
