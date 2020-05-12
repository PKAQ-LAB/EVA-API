package io.nerv.properties

/**
 * license 配置
 *
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:39
 */
class License {
    // 是否启用认证授权
    var enable = false

    // 公钥别名
    var alias: String? = null

    // 该密码是访问密钥库的密码 — 使用 keytool 生成密钥对时设置，使用者知道该密码
    var keystorePwd: String? = null

    // 公共库路径（放在 resource 目录下）
    var path: String? = null

    // 证书路径
    var license: String? = null

    // 项目名
    var subject: String? = null
}