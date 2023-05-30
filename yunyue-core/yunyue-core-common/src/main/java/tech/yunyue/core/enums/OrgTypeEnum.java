package tech.yunyue.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 组织类型
 */
@Getter
@AllArgsConstructor
public enum OrgTypeEnum implements BizCode {
    GROUP("集团", "0001"),
    COMPANY("公司", "0002"),
    DEPARTMENT("部门", "0003");

    /**
     * 名称
     */
    private String msg;
    /**
     * 索引
     */
    private String code;

    /**
     * 是否越级
     * @param higherLevelCode 上一级的类型
     * @param code 当前类型
     */
    public static boolean isLeapFrogging(String higherLevelCode,String code){
        OrgTypeEnum higherLevel = getByCode(higherLevelCode).orElse(OrgTypeEnum.GROUP);
        OrgTypeEnum orgTypeEnum = getByCode(code).orElse(OrgTypeEnum.GROUP);
        return  orgTypeEnum.ordinal() <= higherLevel.ordinal();
    }

    public static Optional<OrgTypeEnum> getByCode(String code){
        return Arrays.stream(OrgTypeEnum.values()).filter(o -> o.getCode().equals(code)).findFirst();
    }
}
