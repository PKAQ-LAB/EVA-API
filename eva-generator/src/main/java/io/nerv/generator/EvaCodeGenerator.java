package io.nerv.generator;

import io.nerv.generator.config.GeneratorConfig;

import java.util.Map;

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
        return Map.of("title", generatorConfig.getTitle(),
                "namespace", generatorConfig.getNamespace(),
                "projectName", generatorConfig.getProjectName(),
                "moduleName", generatorConfig.getModuleName(),
                "pageable", generatorConfig.isPageable(),
                "selection", generatorConfig.isSelection());
    }
}
