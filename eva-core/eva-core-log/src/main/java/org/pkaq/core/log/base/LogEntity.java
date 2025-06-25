package org.pkaq.core.log.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LogEntity {
    /**
     * 创建人岗位ID
     */
    @Schema(description = "创建人岗位ID")
    private long postId;
    /**
     * 创建人部门ID
     */
    @Schema(description = "创建人部门ID")
    private long orgId;
    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private long createId;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private long tenantId;
}
