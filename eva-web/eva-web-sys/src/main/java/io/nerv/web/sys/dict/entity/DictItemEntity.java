package io.nerv.web.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotBlank;

/**
 * 字典管理主表
 * @author S.PKAQ
 */
@Data
@Alias("dictItem")
@TableName("sys_dict_item")
public class DictItemEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @NotBlank(message = "主表ID不允许为空")
    @ApiModelProperty(value = "主表ID", required = true)
    private String mainId;

    @NotBlank(message = "字典项不允许为空")
    @ApiModelProperty(value = "字典项键",required = true)
    private String keyName;

    @NotBlank(message = "字典值不允许为空")
    @ApiModelProperty(value = "字典项值",required = true)
    private String keyValue;

    @ApiModelProperty("输出排序")
    private Integer orders;

    @ApiModelProperty("是否启用")
    private String status;

    private static final long serialVersionUID = 1L;

}