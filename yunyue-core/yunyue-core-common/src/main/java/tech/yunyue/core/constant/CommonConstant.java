package tech.yunyue.core.constant;


/**
 * 系统内置常量
 */
public interface CommonConstant {
    /**
     * 有效记录标识
     */
    String EFFECTIVE_RECORD = "0000";
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
    String X_GATEWAY_VALUE = "yunyue-gateway-request";
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
    // 用户操作的模块id请求头
    String MODULE_ID = "mid";
    // 把用户角色保存在redis中的前缀
    String REDIS_USER_ROLES_PREFIX_KEY = "USER_ROLES:";
    // 把全局角色-权限保存在redis中的前缀
    String REDIS_ROLES_PERMISSION_PREFIX_KEY = "GLOBAL:ROLE:PERMISSION:";
    // 用户登录密码错误失败key
    String REDIS_USER_LOGIN_FAIL_KEY = "USER:LOGIN:FAIL:";
    // 锁定标记：用户限制登录30分钟key
    String REDIS_USER_NO_LOGIN_KEY = "USER:NO:LOGIN:";
    // 黑名单数据
    String CACHE_BLACKDATA = "blackdata";
    // 把全局资源code保存在redis中的前缀
    String REDIS_RESOURCE_CODE_PREFIX_KEY = "GLOBAL:RESOURCE:CODE:";
    // 组织类型-字典标识
    String ORGANIZATION_TYPE_CODE = "organization_type";
    // 把用户保存在redis中的前缀
    String REDIS_USER_INFO_PREFIX_KEY = "USER_INFO:";

    //业务字典的code
    String BIZ_DICT_CODE = "biz";
    //踢出用户事件
    String KICK_USER_EVENT = "kick_user_event";

}
