package io.nerv.generator;

import io.nerv.generator.models.ListModalGenerator;
import io.nerv.web.entity.GeneratorConfig;
import io.nerv.web.entity.TableConfig;
import io.nerv.web.entity.TableField;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EvaCodeGenerator {

    public static final String projectPath = System.getProperty("user.dir");
    // 生成的目标路径
    public static final String targetPath = "/eva-generator/src/generated/ui/";
    // 模板路径
    public static final String templatePath = "templates/mybatis/listModal/ui/";

    public static void main(String[] args) {
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setTitle("日志");
        generatorConfig.setProjectName("sys");
        generatorConfig.setModuleName("log");
        generatorConfig.setNamespace("log");

        TableField tableField = new TableField();
        tableField.setColumnName("log_biz");
        tableField.setCodeMapping("biz");
        tableField.setComment("日志");
        tableField.setCompomentType("0002");
        tableField.setListShow(true);
        tableField.setSearchAble(true);

        TableConfig tableConfig = new TableConfig();
                    tableConfig.setFields(List.of(tableField));

        generatorConfig.setMainTableConfig(tableConfig);

        EvaCodeGenerator evaCodeGenerator = new EvaCodeGenerator();
        Map<String, Object> params = evaCodeGenerator.initParams(generatorConfig);
        // 列表弹窗式生成器
        new ListModalGenerator().generate(generatorConfig.getNamespace(), params);
    }

    /**
     * 初始化模板参数
     * @param generatorConfig
     * @return
     */
    private Map<String, Object> initParams(GeneratorConfig generatorConfig){
        List<TableField> tableFields = generatorConfig.getMainTableConfig().getFields();
        return Map.of("title", generatorConfig.getTitle(),
                "namespace", generatorConfig.getNamespace(),
                "projectName", generatorConfig.getProjectName(),
                "moduleName", generatorConfig.getModuleName(),
                "pageable", generatorConfig.isPageable(),
                "selection", generatorConfig.isSelection(),
                "columns", this.columnInfo(tableFields),
                "search", this.searchInfo(tableFields));
    }

    /**
     * 搜索条件
     * @param columns
     * @return
     */
    private List<TableField> searchInfo(List<TableField> columns){
        return columns.stream()
               .filter(item -> item.isSearchAble())
               .collect(Collectors.toList());
    }

    private List<TableField> columnInfo(List<TableField> columns){
        return columns.stream()
                .filter(item -> item.isListShow())
                .collect(Collectors.toList());
    }
}
