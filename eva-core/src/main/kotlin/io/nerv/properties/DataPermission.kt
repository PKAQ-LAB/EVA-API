package io.nerv.properties

/**
 * 数据权限配置类
 */
class DataPermission {
    /** 是否启用  */
    var enable = false

    /** 需要排除得表  */
    var excludeTables: List<String>? = null

    /** 需要排除得语句  */
    var excludeStatements: List<String>? = null
}