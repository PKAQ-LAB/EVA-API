package io.nerv.properties;

import lombok.Data;

import java.util.List;

/**
 * 数据权限配置类
 */
@Data
public class DataPermission {
    /** 需要排除得表 **/
    private List<String> exclude;
}
