package io.nerv.core.constant;


/**
 * 系统内置常量
 */
public interface CommonConstant {
    // 默认分页条数
    Integer PAGE_SIZE = 10;
    String ADMIN_ROLE_NAME = "ROLE_ADMIN";
    // 鉴权字符串常量
    String TOKEN_KEY = "auth_token";
    String ACCESS_TOKEN_KEY = "access_token";
    String REFRESH_TOKEN_KEY = "refresh_token";
    // 网关转发后下发的用户信息请求头
    String X_CLIENT_TOKEN_USER = "x-client-token-user";
    // 网关转发后下发的权限信息请求头
    String X_CLIENT_TOKEN_ROLES = "x-client-token-roles";
    // 网关加入的请求头
    String X_GATEWAY_HEADER = "x-request";
    // 网关加入的请求串
    String X_GATEWAY_VALUE = "eva-gateway-request";
    // JWT 存储用户id的字符串
    String JWT_USER_ID_STR = "userId";
    // JWT 存储用户name的字符串
    String JWT_USER_NAME_STR = "userName";
    // JWT 存储权限的字符串
    String JWT_USER_ROLES_STR = "authorities";

    String BLOCK_PREFIX = "JWTBLOCK:";
    // 用户信息字符串常量
    String USER_KEY = "user_info";
    // 文件缓存前缀
    String FILE_CACHE_PREFIX = "FILE_TMP_";
    // cachename
    // 文件上传
    String CACHE_UPLOADFILES = "uploadfiles";
    // 重复提交
    String CACHE_REPEATSUBMIT = "repeatsubmit";
    //业务数据
    String CACHE_BIZDATA = "bizdata";
    //字典数据
    String CACHE_DICTDATA = "dictdata";
    // token缓存
    String CACHE_TOKEN = "token";
    //业务用
    String UNKNOWN = "unknown";
    // 请求来源设备类型
    String DEVICE = "device";
    // 请求来源版本号
    String VERSION = "version";
}
