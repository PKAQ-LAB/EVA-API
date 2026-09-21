package org.pkaq.core.tenant;

/**
 * 服务端可信租户 schema 上下文；不得从请求参数直接写入。
 *
 * @author PKAQ
 */
public final class TenantContext {
    private static final ThreadLocal<State> CONTEXT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void bind(Long tenantId, String schemaName) {
        CONTEXT.set(new State(tenantId, schemaName));
    }

    public static Long tenantId() {
        State state = CONTEXT.get();
        return state == null ? null : state.tenantId();
    }

    public static String schemaName() {
        State state = CONTEXT.get();
        return state == null ? null : state.schemaName();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    private record State(Long tenantId, String schemaName) {
    }
}
