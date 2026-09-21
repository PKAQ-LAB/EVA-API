package org.pkaq.core.mybatis.tenant;

import org.springframework.core.Ordered;

/**
 * 租户 schema 路由与事务拦截器的显式顺序。
 *
 * @author PKAQ
 */
public final class TenantRoutingOrder {
    /** 事务必须作为外层 advisor，先建立事务。 */
    public static final int TRANSACTION = Ordered.LOWEST_PRECEDENCE - 100;

    /** schema 路由在事务建立后执行。 */
    public static final int SCHEMA_ROUTING = Ordered.LOWEST_PRECEDENCE - 90;

    private TenantRoutingOrder() {
    }
}
