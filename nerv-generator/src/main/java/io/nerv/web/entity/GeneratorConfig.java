package io.nerv.web.entity;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  包配置
 */
@Data
@Accessors(chain = true)
public class GeneratorConfig {
    /** 名称 **/
    private String title;

    /**  dva name space **/
    private String namespace;

    /** 描述 **/
    private String description;

    /** projectName **/
    private String projectName;

    /** moduleName **/
    private String moduleName;

    /** packageName **/
    private String packageName;

    /** orm选型 **/
    private String orm;

    /** 是否分页 **/
    private boolean pageable;

    /** 是否多选 **/
    private boolean selection;

    /** 界面模型 **/
    private String ui;

    /** 主表名称 **/
    private String mainTableName;

    /** 主表配置 **/
    private TableConfig mainTableConfig;

    /** 子表配置 **/
    private Map<String, TableConfig> childTables;

    /** Domain类名 **/
    public String getDomainName(){
        return StrUtil.upperFirst(moduleName)+"Domain";
    }

    /** Repository **/
    public String getRepoName(){
        return StrUtil.upperFirst(moduleName)+"Repo";
    }

    /** Service类名 **/
    public String getServiceName(){
        return StrUtil.upperFirst(moduleName)+"Service";
    }

    /** controller **/
    public String getCtrlName(){
        return StrUtil.upperFirst(moduleName)+"Ctrl";
    }

    /** 返回子表类名 **/
    public List<String> getChilds(){
        return childTables.keySet().stream()
                          .map(item -> {
                                    String tempStr = StrUtil.subAfter(item, "_", true);
                                    tempStr = StrUtil.toCamelCase(tempStr);
                                    return StrUtil.upperFirst(tempStr);
                          })
                          .collect(Collectors.toList());
    }

    public String getNamespace(){
        if (StrUtil.isBlank(namespace)){
            return projectName+"_"+moduleName;
        }
        return this.namespace;
    }
}
