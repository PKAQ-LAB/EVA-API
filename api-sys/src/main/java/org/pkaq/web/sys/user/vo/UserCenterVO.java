package org.pkaq.web.sys.user.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.web.sys.user.entity.UserEntity;

import java.util.List;
import java.util.Map;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/10/28 18:08
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class UserCenterVO extends UserEntity {

    @ApiModelProperty("权限分组名称")
    private String group;

    @ApiModelProperty("消息数目")
    private int notifyCount = 12;

    @ApiModelProperty("个人标签")
    private List<Map<String, String>> tags;
}
