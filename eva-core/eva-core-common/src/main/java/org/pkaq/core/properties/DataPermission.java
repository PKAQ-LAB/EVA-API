package org.pkaq.core.properties;

import lombok.Data;

import java.util.List;

/**
 * 数据权限配置类
 *
 * @author PKAQ
 */
@Data
public class DataPermission {
    /**
     * 是否启用
     **/
    private boolean enable;

    /**
     * 需要排除得表
     **/
    private List<String> excludeTables;

    /**
     * 需要排除得语句
     **/
    private List<String> excludeStatements;
}
