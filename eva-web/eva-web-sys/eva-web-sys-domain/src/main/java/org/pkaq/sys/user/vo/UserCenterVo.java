package org.pkaq.sys.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

import java.util.List;
import java.util.Map;

/**
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserCenterVo extends StdVo {

    @Schema(description = "权限分组名称")
    private String group;

    @Schema(description = "消息数目")
    private int notifyCount = 12;

    @Schema(description = "个人标签")
    private List<Map<String, String>> tags;
}
