package tech.yunyue.param.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author dmz
 */
@Schema(description="系统参数设置详情视图对象")
@Data
@EqualsAndHashCode(callSuper=false)
public class SystemParameterDetailVo  {
    /**
     * 记录id
     */
    @Schema(description = "记录id")
    private String id;
    /**
     * 系统参数值
     */
    @Schema(description = "系统参数值")
    private String codeVal;
    /**
     * 参数编码
     */
    @Schema(description = "参数编码")
    private String code;

}
