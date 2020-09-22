package io.nerv.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum ResponseEnumm {
    /**
     * 可用
     */
    OPERATE_SUCCESS("操作成功"),
    SAVE_SUCCESS("保存成功"),
    EDIT_SUCCESS("编辑成功"),
    DELETE_SUCCESS("删除成功"),
    QUERY_SUCCESS("查询成功"),
    NULL_MSG(null),
    OPERATE_FAILED("操作失败");

    /**
     * 名称
     */
    private String name;
}
