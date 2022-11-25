package io.nerv.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: S.PKAQ
 */
@Getter
@AllArgsConstructor
public enum DataPermissionEnumm implements BizCode {
    /**
     * 可用
     */
    ALL("全部权限", "0000"),
    DEPT_ONLY_LIMIT("仅本部门", "0001"),
    DEPT_AND_CHILDREN_LIMIT("本人所属部门及下属部门", "0002"),
    DEPT_LIMIT("指定部门", "0003"),
    CREATOR_LIMIT("本人创建或修改", "0005");

    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private String code;
}
