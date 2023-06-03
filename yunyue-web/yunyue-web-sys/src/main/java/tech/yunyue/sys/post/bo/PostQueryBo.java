package tech.yunyue.sys.post.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author dmz
 */
@Schema(description = "岗位列表请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class PostQueryBo {
    /**
     * 岗位
     */
    @Schema(description = "岗位")
    private String title;
    /**
     * 职级
     */
    @Schema(description = "职级")
    private String level;

}
