package org.pkaq.core.mybatis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pkaq.core.codes.BizCode;

import java.util.Arrays;

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
    POST_ONLY_LIMIT("仅本岗位", "0004"),
    POST_AND_CHILDREN_LIMIT("本人所属岗位及下属岗位", "0005"),
    POST_LIMIT("指定岗位", "0006"),
    CREATOR_LIMIT("本人创建或修改", "0007");

    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private String code;

    public static DataPermissionEnumm getByCode(String code) {
        return Arrays.stream(DataPermissionEnumm.values()).filter(o -> o.getCode().equals(code)).findFirst().orElse(null);
    }

}
