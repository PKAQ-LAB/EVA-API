-- 为 log_error 表添加租户隔离字段
ALTER TABLE log_error ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT NULL;

COMMENT ON COLUMN log_error.tenant_id IS '租户ID';

-- 租户隔离索引
CREATE INDEX IF NOT EXISTS IDX_LOG_ERROR_TENANT_ID ON log_error(tenant_id);
-- 请求时间索引（排序 + 范围查询）
CREATE INDEX IF NOT EXISTS IDX_LOG_ERROR_REQUEST_TIME ON log_error(request_time);
-- 租户 + 请求时间联合索引
CREATE INDEX IF NOT EXISTS IDX_LOG_ERROR_TENANT_TIME ON log_error(tenant_id, request_time);
