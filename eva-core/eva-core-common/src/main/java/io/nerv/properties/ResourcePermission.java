package io.nerv.properties;

import lombok.Data;

/**
 * 资源权限配置类
 */
@Data
public class ResourcePermission {
    /** 是否启用 **/
    private boolean enable;

    /** 是否采用严格鉴权模式 **/
    private boolean strict;
}
