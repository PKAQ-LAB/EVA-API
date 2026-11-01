package org.pkaq.core.auth.adapter.mybatis.audit.mapper;

import org.pkaq.core.mybatis.log.LoginLogQueryScope;

/**
 * 每次构建 SQL 都核验登录日志窗口，关闭数据插件也不能绕过。
 * @author PKAQ
 * @date 2026-10-30
 */
public final class LoginLogSqlProvider {
    private static final String MAPPER = LoginLogMapper.class.getName() + ".";

    private LoginLogSqlProvider() {
    }

    /** 详情始终包含授权窗口固定的租户条件。 */
    public static String detail() {
        return "SELECT * FROM SYS_LOGIN_LOG WHERE ID = #{id}" + tenantCondition("selectHistoryDetail");
    }

    /** 分页筛选只缩小固定的授权范围，分页插件从相同 SQL 生成 count。 */
    public static String page() {
        return "<script>SELECT * FROM SYS_LOGIN_LOG WHERE 1 = 1" + tenantCondition("selectHistoryPage") + """
                <if test="query.account != null and query.account.trim() != ''">
                    AND ACCOUNT LIKE CONCAT('%', #{query.account}, '%')
                </if>
                <if test="query.success != null">AND SUCCESS = #{query.success}</if>
                <if test="query.sessionId != null and query.sessionId.trim() != ''">
                    AND SESSION_ID = #{query.sessionId}
                </if>
                <if test="query.ip != null and query.ip.trim() != ''">AND IP = #{query.ip}</if>
                <if test="query.riskFlag != null and query.riskFlag.trim() != ''">
                    AND RISK_FLAGS LIKE CONCAT('%', #{query.riskFlag}, '%')
                </if>
                <if test="query.begin != null">AND UTC_CREATE &gt;= #{query.begin}</if>
                <if test="query.end != null">AND UTC_CREATE &lt;= #{query.end}</if>
                ORDER BY UTC_CREATE DESC</script>
                """;
    }

    /** 风险设备计数只在系统存储窗口内按原有可信条件执行。 */
    public static String riskCount() {
        LoginLogQueryScope.requireStorage(MAPPER + "selectRiskDeviceCount");
        return """
                SELECT COUNT(*) FROM SYS_LOGIN_LOG WHERE TENANT_ID = #{tenantId}
                AND USER_ID = #{userId} AND SUCCESS = TRUE AND DEVICE_FINGERPRINT = #{fingerprint}
                """;
    }

    /** 最近登录查询不借用人类管理员资格。 */
    public static String riskLast() {
        LoginLogQueryScope.requireStorage(MAPPER + "selectRiskLastLogin");
        return """
                SELECT * FROM SYS_LOGIN_LOG WHERE TENANT_ID = #{tenantId}
                AND USER_ID = #{userId} AND SUCCESS = TRUE AND LOGIN_TYPE = 'PASSWORD'
                ORDER BY UTC_CREATE DESC LIMIT 1
                """;
    }

    private static String tenantCondition(String method) {
        Long tenantId = LoginLogQueryScope.requireHistory(MAPPER + method);
        // 数值只来自已核验的同步授权窗口，查询对象不能删除或替换此条件。
        return tenantId == null ? "" : " AND TENANT_ID = " + tenantId;
    }
}
