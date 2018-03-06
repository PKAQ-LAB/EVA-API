package org.pkaq.core.enums;

import lombok.Getter;

/**
 * HTTP状态码
 */
@Getter
public enum  HttpCodeEnum {
    QUERY_SUCCESS("服务器成功返回请求的数据", 200),
    EDIT_SUCCESS("新建或修改数据成功", 201),
    POST_SUCCESS("一个请求已经进入后台排队（异步任务）。", 202),
    DEL_SUCCESS("删除数据成功。", 204),
    REQEUST_FAILURE("请求参数错误。", 400),
    ROLE_ERROR("用户没有权限（令牌、用户名、密码错误）", 401),
    BAD_METHOD("不支持当前请求方法 ", 405),
    BAD_MEDIATYPE("不支持当前媒体类型 ", 415),
    REQEUST_REFUSED("用户得到授权，但是访问是被禁止的。", 403),
    SERVER_ERROR("服务器发生错误，请检查服务器。", 500),
    SERVICE_ERROR("服务不可用，服务器暂时过载或维护。", 503),
    GATE_TIMEOUT("网关超时。", 504);

    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private int index;

    HttpCodeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(int index) {
        for (HttpCodeEnum p : HttpCodeEnum.values()) {
            if ( index == p.getIndex() ) {
                return p.name;
            }
        }
        return "-";
    }
}
