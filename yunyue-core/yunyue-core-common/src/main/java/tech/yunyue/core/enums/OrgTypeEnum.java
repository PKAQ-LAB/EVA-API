package tech.yunyue.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.yunyue.core.exception.BizException;

import java.util.Arrays;
import java.util.Objects;
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
        OrgTypeEnum higherLevel = getByCode(higherLevelCode);
        OrgTypeEnum orgTypeEnum = getByCode(code);
        //只能向下创建 但是部门可以创建部门
        return  !(higherLevel == DEPARTMENT && higherLevel == orgTypeEnum) && orgTypeEnum.ordinal() <= higherLevel.ordinal()  ;
    }

    public static OrgTypeEnum getByCode(String code){
        OrgTypeEnum orgTypeEnum = Arrays.stream(OrgTypeEnum.values()).filter(o -> o.getCode().equals(code)).findFirst().orElse(null);
        if(Objects.isNull(orgTypeEnum)){
            BizCodeEnum.ORG_TYPE_NO_EXIST.newException(code);
        }
        return orgTypeEnum;
    }
}
