package io.nerv.core.properties;

import lombok.Data;

/**
 * 访问鉴权配置
 */
@Data
public class Security {
    private String[] webstatic;

    // 无需鉴权路径(无条件访问)
    private String[] anonymous;

    // 无需资源鉴权的路径
    private String[] permit;
}
