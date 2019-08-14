package io.nerv.core.enums;

import lombok.Getter;

/**
 * 错误状态码 统一为 4xxx
 * @author PKAQ
 */
@Getter
public enum ErrorCodeEnum {
    /**
     * 请求成功
     */
    PARAM_ERROR("请求参数错误.", 4000),
    PARAM_LOST("请求参数丢失.", 4001),
    PARAM_TYPEERROR("参数类型错误 ", 4002),
    PARAM_LENGTH("参数长度错误", 4003),
    SERVER_ERROR("服务器发生错误,请联系管理员.", 4004);

    /**
     * 名称
     */
    private String name;
    /**
     * 索引
     */
    private int index;

    ErrorCodeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(int index) {
        for (ErrorCodeEnum p : ErrorCodeEnum.values()) {
            if ( index == p.getIndex() ) {
                return p.name;
            }
        }
        return "-";
    }
}