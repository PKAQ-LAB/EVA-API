package io.nerv.core.constant;


/**
 * 系统内置常量
 */
public interface CommonConstant {
    // 默认分页条数
    Integer PAGE_SIZE = 10;

    // 鉴权字符串常量
    String TOKEN_KEY = "auth_token";
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

}
