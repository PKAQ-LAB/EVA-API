package io.nerv.properties

/**
 * 文件上传配置类
 */
class Upload {
    // 使用的存储类型
    var type: String? = null

    // 上传存储临时路径
    var tempPath: String? = null

    // 上传存储路径
    var storagePath: String? = null

    // 后缀名集
    var allowSuffixName: String? = null

    // DFS服务器地址
    var serverUrl: String? = null
}