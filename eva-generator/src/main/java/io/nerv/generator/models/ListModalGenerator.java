package io.nerv.generator.models;

import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import io.nerv.generator.EvaCodeGenerator;

import java.io.File;
import java.util.Map;

/**
 * 列表弹窗式生成器
 */
public class ListModalGenerator implements Generator {
    // 生成文件
    @Override
    public void generate(String namespace, Map<String, Object> params){
        TemplateConfig templateConfig = new TemplateConfig("templates", TemplateConfig.ResourceMode.CLASSPATH);
        TemplateEngine engine = TemplateUtil.createEngine (templateConfig);
        System.out.println("[前端] 代码生成开始 --------------------------------------- > ");
        // 入口页面
        Template index = engine.getTemplate(EvaCodeGenerator.templatePath + "index.jsx.vm");
        File index_file = new File(EvaCodeGenerator.projectPath + EvaCodeGenerator.targetPath + namespace + "/index.jsx");
        index.render(params, index_file);

        // 列表页面
        Template list = engine.getTemplate(EvaCodeGenerator.templatePath + "list.jsx.vm");
        File list_file = new File(EvaCodeGenerator.projectPath + EvaCodeGenerator.targetPath + namespace + "/list.jsx");
        list.render(params, list_file);

        // aoe页面
        Template aoeform = engine.getTemplate(EvaCodeGenerator.templatePath + "aoeform.jsx.vm");
        File aoe_file = new File(EvaCodeGenerator.projectPath + EvaCodeGenerator.targetPath + namespace + "/aoeform.jsx");
        aoeform.render(params, aoe_file);

        // model
        Template mdl = engine.getTemplate(EvaCodeGenerator.templatePath + "models/Mdl.js.vm");
        File mdl_file = new File(EvaCodeGenerator.projectPath + EvaCodeGenerator.targetPath + namespace + "/models/" + namespace + "Mdl.js");
        mdl.render(params, mdl_file);

        // service
        Template svc = engine.getTemplate(EvaCodeGenerator.templatePath + "services/Svc.js.vm");
        File svc_file = new File(EvaCodeGenerator.projectPath + EvaCodeGenerator.targetPath + namespace + "/services/" + namespace + "Svc.js");
        svc.render(params, svc_file);
        System.out.println("[前端] ; ) 代码生成完毕 --------------------------------------- > ");
    }
}
