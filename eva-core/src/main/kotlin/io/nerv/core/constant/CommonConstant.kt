package io.nerv.core.constant

/**
 * 系统内置常量
 */
interface CommonConstant {

    companion object {
        // 默认分页条数
        const val PAGE_SIZE = 10

        // 鉴权字符串常量
        const val TOKEN_KEY = "auth_token"

        // 用户信息字符串常量
        const val USER_KEY = "user_info"

        // 文件缓存前缀
        const val FILE_CACHE_PREFIX = "FILE_TMP_"

        // cachename
        // 文件上传
        const val CACHE_UPLOADFILES = "uploadfiles"

        // 重复提交
        const val CACHE_REPEATSUBMIT = "repeatsubmit"

        //业务数据
        const val CACHE_BIZDATA = "bizdata"

        //字典数据
        const val CACHE_DICTDATA = "dictdata"

        // token缓存
        const val CACHE_TOKEN = "token"

        //业务用
        const val UNKNOWN = "unknown"

        // 请求来源设备类型
        const val DEVICE = "device"

        // 请求来源版本号
        const val VERSION = "version"
    }
}