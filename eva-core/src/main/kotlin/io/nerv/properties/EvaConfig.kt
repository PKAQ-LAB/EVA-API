package io.nerv.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * 业务日志配置读取类
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 21:06
 */
@Configuration
@ConfigurationProperties(prefix = "eva")
@EnableConfigurationProperties(EvaConfig::class)
open class EvaConfig {
    /**是否启用重复提交判断  */
    var norepeatCheck = false

    /**业务日志配置  */
    var bizlog: BizLog? = null
        get() = field ?: BizLog()

    /**业务日志配置  */
    var errorLog: ErrorLog? = null
        get() = field ?: ErrorLog()

    /** 文件上传配置  */
    var upload: Upload? = null
        get() = field ?: Upload()

    /** jwt配置  */
    var jwt: Jwt? = null
        get() = field ?: Jwt()

    /** cookie 配置  */
    var cookie: Cookie? = null
        get() = field ?: Cookie()

    /** 访问鉴权配置  */
    var security: Security? = null

    /** 数据权限配置  */
    var dataPermission: DataPermission? = null
        get() = field ?: DataPermission()

    /** 资源权限配置  */
    var resourcePermission: ResourcePermission? = null
        get() = field ?: ResourcePermission()

    /** 是否启用license 授权机制  */
    var license: License? = null
        get() = field ?: License()

    /** 缓存配置  */
    var cache: Cache? = null
        get() = field ?: Cache()

}
