package io.nerv.web.sys.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.nerv.web.sys.user.entity.UserEntity;

import java.util.List;
import java.util.Map;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/10/28 18:08
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class UserCenterVO extends UserEntity {

    @Schema(description = "权限分组名称")
    private String group;

    @Schema(description = "消息数目")
    private int notifyCount = 12;

    @Schema(description = "个人标签")
    private List<Map<String, String>> tags;
}
