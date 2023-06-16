package tech.yunyue.sys.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统参数的名称
 */
@Getter
@AllArgsConstructor
public enum SystemParamEnum {
    /**
     * 执手
     */
    HOLD_HANDS("hold_hands"),
    /**
     * 默认玻璃颜色
     */
    GLASS_COLOR("glass_color"),
    /**
     * 玻璃默认插入量
     */
    GLASS_INSERTION("glass_insertion"),
    /**
     * 执手离地高度
     */
    HOLDING_HEIGHT("holding_height"),
    /**
     * 余料保留长度
     */
    MATERIAL_HEIGHT("material_height"),
    /**
     * 料头长度
     */
    DEFAULT_HEAD_HEIGHT("default_head_height"),
    /**
     * 业务字典
     */
    BIZ_DICT("biz_dict");
    /**
     * 参数名称
     */
    private String name;
}



