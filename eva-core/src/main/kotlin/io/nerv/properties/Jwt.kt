package io.nerv.properties

/**
 * jwt配置
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:39
 */
class Jwt {
    //刷新TOKEN(有返回数据)
    //int rescode_reftoken_msg = 1006;
    ////刷新TOKEN
    //int rescode_reftoken = 1007;
    /** 是否持久化  */
    var persistence = false

    /**  token header参数名  */
    var header = "Authorization"

    /**  token 前缀  */
    var tokenHead = "Bearer"

    /**  签发人  */
    var sign = "PKAQ"

    /** 密匙  */
    var secert = "aHR0cDovL3BrYXEub3Jn"

    /** token有效时间  */
    var ttl = 30 * 24 * 60 * 60 * 1000.toLong()

    /** 续期时间  */
    var threshold = 60 * 60 * 24 * 1000.toLong()

    /** 可信任域  */
    var creditUrl: List<String>? = null
}