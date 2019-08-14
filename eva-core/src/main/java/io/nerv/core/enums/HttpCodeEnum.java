package io.nerv.core.enums;

import lombok.Getter;

/**
 * HTTP状态码
 * @author PKAQ
 */
@Getter
public enum  HttpCodeEnum {
    /**
     * 请求成功
     */
    QUERY_SUCCESS("服务器成功返回请求的数据", 200),
    EDIT_SUCCESS("新建或修改数据成功", 201),
    POST_SUCCESS("一个请求已经进入后台排队（异步任务）。", 202),
    DEL_SUCCESS("删除数据成功.", 204),
    REQEUST_FAILURE("请求参数错误.", 400),
    ROLE_ERROR("您的登录已失效,请重新登录.", 401),
    BAD_METHOD("不支持当前请求方法 ", 405),
    BAD_MEDIATYPE("不支持当前媒体类型 ", 415),
    REQEUST_REFUSED("操作权限不足.", 403),
    SERVER_ERROR("服务器发生错误,请联系管理员.", 500),
    SERVICE_ERROR("服务不可用,服务器暂时过载或维护.", 503),
    RULECHECK_FAILED("规则校验未通过.", 510),
    GATE_TIMEOUT("网关超时.", 504);

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