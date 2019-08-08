package io.nerv.web.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel("代码生成器通用配置")
public class GeneratorEntity {
    @ApiModelProperty("名称")
    private String title;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("projectName")
    private String projectName;

    @ApiModelProperty("moduleName")
    private String moduleName;

    @ApiModelProperty("packageName")
    private String packageName;

    @ApiModelProperty("orm选型")
    private String orm;

    @ApiModelProperty("是否分页")
    private boolean pageable;

    @ApiModelProperty("是否多选")
    private boolean selection;

    @ApiModelProperty("界面模型")
    private String ui;

    @ApiModelProperty("主表名称")
    private String mainTableName;

    @ApiModelProperty("主表配置")
    private TableConfig mainTableConfig;

    @ApiModelProperty("子表配置")
    private Map<String, TableConfig> childTables;

}
