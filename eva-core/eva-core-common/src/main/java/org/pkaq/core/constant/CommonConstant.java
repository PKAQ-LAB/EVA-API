package org.pkaq.core.constant;


import lombok.experimental.UtilityClass;

/**
 * 系统内置常量
 *
 * @author PKAQ
 */
@UtilityClass
public class CommonConstant {

    public final static String AUTH_PREFIX = "ROLE_";
    public final static String MODE_STANDALONE = "standalone";
    public final static String MODE_PLATFORM = "platform";
    public final static String MODE_SAAS = "saas";
    /** 兼容旧配置值，新增配置应使用 MODE_STANDALONE。 */
    public final static String MODE_SINGLETON = "singleton";
    /**
     * 有效记录标识
     */
    public final static String EFFECTIVE_RECORD = "0000";
    // 默认分页条数
    Integer PAGE_SIZE = 10;
    public final static String ADMIN_ROLE_NAME = "ROLE_ADMIN";
    // 鉴权字符串常量
    public final static String TOKEN_KEY = "auth_token";
    public final static String ACCESS_TOKEN_KEY = "access_token";
    public final static String REFRESH_TOKEN_KEY = "refresh_token";
    // 网关转发后下发的用户信息请求头
    public final static String X_CLIENT_TOKEN_USER = "x-client-token-user";
    // 网关转发后下发的权限信息请求头
    public final static String X_CLIENT_TOKEN_ROLES = "x-client-token-roles";
    // 网关加入的请求头
    public final static String X_GATEWAY_HEADER = "x-request";
    // 网关加入的请求串
    public final static String X_GATEWAY_VALUE = "eva-gateway-request";
    // JWT 存储用户id的字符串
    public final static String JWT_USER_ID_STR = "userId";
    // JWT 存储用户name的字符串
    public final static String JWT_USER_NAME_STR = "userName";
    // JWT 存储权限的字符串
    public final static String JWT_USER_ROLES_STR = "authorities";

    public final static String BLOCK_PREFIX = "JWTBLOCK:";
    // 用户信息字符串常量
    public final static String USER_KEY = "user_info";
    // 文件缓存前缀
    public final static String FILE_CACHE_PREFIX = "FILE_TMP_";
    // cachename
    // 文件上传
    public final static String CACHE_UPLOADFILES = "uploadfiles";
    // 重复提交
    public final static String CACHE_REPEATSUBMIT = "repeatsubmit";
    //业务数据
    public final static String CACHE_BIZDATA = "bizdata";
    //字典数据
    public final static String CACHE_DICTDATA = "dictdata";
    // token缓存
    public final static String CACHE_TOKEN = "token";
    //业务用
    public final static String UNKNOWN = "unknown";
    // 请求来源设备类型
    public final static String DEVICE = "device";
    // 请求来源版本号
    public final static String VERSION = "version";

    // 用户操作的模块id请求头
    public final static String MODULE_ID = "mid";
    // 用户操作的模块code请求头
    public final static String MODULE_CODE = "mcode";

    public final static String SYS_ALL_DICT_KEY = "'sys:all:dict'";
}
