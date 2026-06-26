package org.pkaq.sys.post.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

/**
 * 岗位查询请求参数
 * <p>
 * 注意：移除了 tenantId 字段，由多租户拦截器接管，避免前端越权查询其它租户数据。
 *
 * @author dmz
 */
@Schema(description = "岗位列表请求参数")
@Data
@EqualsAndHashCode(callSuper = true)
public class PostQueryBo extends PageBo {
    @Schema(description = "编码")
    private String code;

    @Schema(description = "岗位名称")
    private String title;

    @Schema(description = "职级")
    private String level;

    @Schema(description = "上级岗位ID（按层级筛选）")
    private Long pid;
}
