package io.nerv.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum ResponseEnumm implements BizCode {
    /**
     * 可用
     */
    OPERATE_SUCCESS("操作成功", "0101"),
    SAVE_SUCCESS("保存成功", "0102"),
    EDIT_SUCCESS("编辑成功", "0103"),
    DELETE_SUCCESS("删除成功", "0104"),
    QUERY_SUCCESS("查询成功", "0105"),
    NULL_MSG(null, "0100"),
    OPERATE_FAILED("操作失败", "0106");
    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private String code;
}
