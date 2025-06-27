package org.pkaq.sys.module.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.Vo;

/**
 * @author PKAQ
 */
@Data
@Schema(title = "模块资源管理")
@EqualsAndHashCode(callSuper = false)
public class ResourcesVo implements Vo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "模块id")
    private Long moduleId;

    @Schema(description = "资源描述")
    private String resourceDesc;

    @Schema(description = "资源路径")
    private String resourceUrl;

    @Schema(description = "资源类型（按钮、操作）")
    private String resourceType;
}