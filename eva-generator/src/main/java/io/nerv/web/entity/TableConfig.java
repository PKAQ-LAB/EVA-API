package io.nerv.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("代码生成器表配置")
public class TableConfig {
    @ApiModelProperty("字段名")
    private String columnName;

    @ApiModelProperty("字段类型")
    private String columnType;

    @ApiModelProperty("字段描述")
    private String comment;

    @ApiModelProperty("字段长度")
    private String length;

    @ApiModelProperty("是否允许为空")
    private boolean nullAble;

    @ApiModelProperty("字典映射")
    private String codeMapping;

    @ApiModelProperty("是否列表显示")
    private boolean listShow;

    @ApiModelProperty("是否表单显示")
    private boolean formShow;

    @ApiModelProperty("是否为查询条件")
    private boolean searchAble;

    @ApiModelProperty("控件类型")
    private String compomentType;

    @ApiModelProperty("显示名称")
    private String showName;
}
