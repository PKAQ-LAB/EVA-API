package io.nerv.generator;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * 单表代码生成器
 * @author PKAQ
 */
public class SingleCodeGenerator {
    // 作者
    private final static String AUTHOR = "PKAQ";
    // 基础包名
    private final static String BASEPKG = "com.hctms.web";
    // 系统名
    private final static String SYS_NAME = "hctms";

    // 数据库配置
    private final static String IP_PORT = "139.219.11.26:3306";
    private final static String DBNAME = "hctms";
    private final static String URL = "jdbc:mysql://"+IP_PORT+"/"+DBNAME+"?useUnicode=true&characterEncoding=UTF-8";
    private final static String DRIVER = "com.mysql.cj.jdbc.Driver";
    private final static String USER = "hctms";
    private final static String PWD = "hctms";
    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/addon-generator/src/main/java");
        gc.setAuthor(AUTHOR);
        // 是否打开输出目录
        gc.setOpen(false);
        // 启用swagger
        gc.setSwagger2(true);
        gc.setServiceName("%sService");
        // 不生成serviceImpl类
        gc.setServiceImplName(null);
        gc.setControllerName("%sCtrl");
        gc.setEntityName("%sEntity");
        mpg.setGlobalConfig(gc);

        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(URL);
        dsc.setDriverName(DRIVER);
        dsc.setUsername(USER);
        dsc.setPassword(PWD);
        mpg.setDataSource(dsc);


        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName("");
            pc.setParent(BASEPKG);
        mpg.setPackageInfo(pc);

        // 模板配置
        TemplateConfig templateConfig = new TemplateConfig();
        // xml模板
        templateConfig.setXml(null);
        // entity模板
        templateConfig.setEntity("/templates/single/entity.java");
        // controller模板
        templateConfig.setController("templates/single/controller.java");
        // mapper模板
        templateConfig.setMapper("/templates/single/mapper.java");
        // service模板
        templateConfig.setService("/templates/single/service.java");
        templateConfig.setServiceImpl(null);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<>(1);
                map.put("sysName", SYS_NAME);
                this.setMap(map);
            }
        };
        mpg.setCfg(cfg);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);

        // 定义基类
        strategy.setSuperEntityClass("io.nerv.core.mvc.entity.StdBaseEntity");
        strategy.setSuperControllerClass("io.nerv.core.mvc.ctrl.StdBaseCtrl");
        strategy.setSuperMapperClass("com.baomidou.mybatisplus.core.mapper.BaseMapper");
        strategy.setSuperServiceClass("io.nerv.core.mvc.service.StdBaseService");

        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
        strategy.setControllerMappingHyphenStyle(true);

        strategy.setInclude("");
        strategy.setSuperEntityColumns("ID","CREATE_BY","GMT_CREATE","MODIFY_BY","GMT_MODIFY","REMARK");
        strategy.setTablePrefix("");

        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new VelocityTemplateEngine());
        mpg.execute();
    }

}