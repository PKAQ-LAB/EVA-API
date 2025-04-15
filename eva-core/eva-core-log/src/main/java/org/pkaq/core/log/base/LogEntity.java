package org.pkaq.core.log.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LogEntity {
    /**
     * 创建人岗位ID
     */
    @Schema(description = "创建人岗位ID")
    private String postId;
    /**
     * 创建人部门ID
     */
    @Schema(description = "创建人部门ID")
    private String orgId;
    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private String createId;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private String tenantId;
}
