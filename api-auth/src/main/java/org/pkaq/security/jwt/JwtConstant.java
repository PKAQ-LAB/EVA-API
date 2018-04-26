package org.pkaq.security.jwt;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/20 15:39
 */
public class JwtConstant {
    ///刷新TOKEN(有返回数据)
    public static final int RESCODE_REFTOKEN_MSG = 1006;
    //刷新TOKEN
    public static final int RESCODE_REFTOKEN = 1007;

    //Token不存在
    public static final int JWT_ERRCODE_NULL = 4000;
    //Token过期
    public static final int JWT_ERRCODE_EXPIRE = 4001;
    //验证不通过
    public static final int JWT_ERRCODE_FAIL = 4002;

    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_TOKENHEAD = "Bearer";

    public static final String SIGN_USER = "PKAQ";
    //密匙
    public static final String JWT_SECERT = "aHR0cDovL3BrYXEub3Jn";
    //token有效时间
    public static final long JWT_TTL = 60 * 60 * 1000;
}
