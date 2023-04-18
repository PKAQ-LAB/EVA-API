package io.nerv.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.ibatis.type.Alias;


/**
 * 字典管理主表
 *
 * @author S.PKAQ
 */
@Data
@Alias("dictItem")
@TableName("sys_dict_item")
public class DictItemEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @NotBlank(message = "主表ID不允许为空")
    @Schema(description = "主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mainId;

    @NotBlank(message = "字典项不允许为空")
    @Schema(description = "字典项键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyName;

    @NotBlank(message = "字典值不允许为空")
    @Schema(description = "字典项值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyValue;

    @Schema(description = "输出排序")
    private Integer orders;

    @Schema(description = "是否启用")
    private String status;

    private static final long serialVersionUID = 1L;

}