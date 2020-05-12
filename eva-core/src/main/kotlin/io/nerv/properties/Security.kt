package io.nerv.properties

/**
 * 访问鉴权配置
 */
class Security {
    // 无需鉴权路径(无条件访问)
    var anonymous: Array<String> = Array(1) {""}

    // 无需资源鉴权的路径
    var permit: Array<String> = Array(1) {""}
}