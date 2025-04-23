package org.pkaq.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.io.Serializable;


/**
 * 字典管理主表
 *
 * @author S.PKAQ
 */
@Data
@Alias("dictItem")
@TableName("sys_dict_item")
@EqualsAndHashCode(callSuper = true)
public class DictItemEntity extends StdEntity implements Serializable {

    @NotBlank(message = "主表ID不允许为空")
    @Schema(description = "主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mainId;

    @NotBlank(message = "字典项不允许为空")
    @Schema(description = "字典项键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dCode;

    @NotBlank(message = "字典值不允许为空")
    @Schema(description = "字典项值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dValue;
}