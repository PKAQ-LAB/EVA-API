package io.nerv.web.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 字典项管理实体类
 * @author S.PKAQ
 */
@Data
@Alias("dict")
@TableName("sys_dict")
@EqualsAndHashCode(callSuper = true)
@ApiModel("字典管理")
public class DictEntity extends StdBaseEntity {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "编码不允许为空")
    @ApiModelProperty("字典分类编码")
    private String code;

    @NotBlank(message = "编码类型不允许为空")
    @ApiModelProperty("字典分类名称")
    private String name;

    @NotBlank(message = "归属类型不允许为空")
    @ApiModelProperty("上级节点")
    private String parentId;

    @ApiModelProperty("是否可用（0 已删除,1 可用）")
    private String status;

    @TableField(exist = false)
    @ApiModelProperty("字典项列表")
    private List<DictItemEntity> lines;

    @TableField(exist = false)
    @ApiModelProperty("子节点")
    private List<DictEntity> children;

}