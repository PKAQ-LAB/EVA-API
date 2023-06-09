package tech.yunyue.sys.param.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author dmz
 */
@Schema(description = "系统参数设置编辑/唯一校验提交参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class SystemParameterEditBo {
    /**
     * 记录id
     */
    @Schema(description = "记录id")
    @NotBlank(message = "记录id不能为空")
    private String id;
    /**
     * 系统参数值
     */
    @Length(max = 40)
    @Schema(description = "系统参数值")
    @NotBlank(message = "系统参数值不能为空")
    private String codeVal;
    /**
     * 乐观锁
     */
    private int revision;
}
