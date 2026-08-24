package org.pkaq.sys.dev.generator.service;

import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.dev.generator.bo.AiCodeFieldBo;
import org.pkaq.sys.dev.generator.bo.AiCodePromptBo;
import org.pkaq.sys.dev.generator.convert.AiCodeGeneratorConvert;
import org.pkaq.sys.dev.generator.vo.AiCodePromptVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI代码生成提示词服务。
 * <p>
 * 该服务只负责把必要业务信息整理成提示词，不再读取模板文件或在服务端生成代码文件。
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class AiCodeGeneratorService {

    private static final String DEFAULT_FEATURE_TYPE = "crud";

    private final AiCodeGeneratorConvert convert;

    /**
     * 生成AI代码提示词。
     *
     * @param bo 提示词参数
     * @return 提示词结果
     */
    public AiCodePromptVo buildPrompt(AiCodePromptBo bo) {
        String title = "生成" + trimToEmpty(bo.getModuleName()) + "代码";
        return this.convert.toVo(title, buildPromptText(bo));
    }

    private String buildPromptText(AiCodePromptBo bo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是资深Java全栈工程师，请基于以下信息为 EVA-API 项目生成代码。").append('\n');
        prompt.append("禁止使用模板文件生成代码，必须直接按项目现有代码风格输出可落地的代码改动。").append('\n');
        prompt.append('\n');

        appendSection(prompt, "一、项目约束");
        prompt.append("1. 后端使用 Java、Spring Boot、MyBatis-Plus、Lombok、MapStruct。").append('\n');
        prompt.append("2. Controller 返回 org.pkaq.core.mvc.vo.Response。").append('\n');
        prompt.append("3. BO/VO/Entity/Service/Mapper/Ctrl 命名必须跟随项目现有规范。").append('\n');
        prompt.append("4. Entity 不得直接暴露给前端，必须使用 VO。").append('\n');
        prompt.append("5. 复杂逻辑必须使用中文注释。").append('\n');
        prompt.append("6. 只生成必要代码，不得引入无关抽象或模板引擎。").append('\n');
        prompt.append('\n');

        appendSection(prompt, "二、业务信息");
        appendLine(prompt, "模块名称", bo.getModuleName());
        appendLine(prompt, "业务说明", bo.getRequirement());
        appendLine(prompt, "功能类型", defaultIfBlank(bo.getFeatureType(), DEFAULT_FEATURE_TYPE));
        appendLine(prompt, "包名", bo.getPackageName());
        appendLine(prompt, "实体类名", bo.getEntityName());
        appendLine(prompt, "数据库表名", bo.getTableName());
        appendLine(prompt, "前端路由", bo.getRouteUrl());
        appendLine(prompt, "补充约束", bo.getExtraConstraints());
        prompt.append('\n');

        appendSection(prompt, "三、字段信息");
        appendFields(prompt, bo.getFields());
        prompt.append('\n');

        appendSection(prompt, "四、输出要求");
        prompt.append("1. 先列出需要新增或修改的文件清单。").append('\n');
        prompt.append("2. 再按文件输出完整代码或精确补丁。").append('\n');
        prompt.append("3. 必须包含必要的 Controller、Service、Mapper、Entity、BO、VO、Convert。").append('\n');
        prompt.append("4. 如涉及数据库，必须提供 PostgreSQL 可执行 SQL。").append('\n');
        prompt.append("5. 如涉及菜单或权限，必须说明需要新增的 SYS_MODULE_RESOURCES 资源。").append('\n');
        prompt.append("6. 最后给出接口测试 JSON 示例。");
        return prompt.toString();
    }

    private void appendSection(StringBuilder prompt, String title) {
        prompt.append(title).append('\n');
    }

    private void appendLine(StringBuilder prompt, String label, String value) {
        if (isBlank(value)) {
            return;
        }
        prompt.append("- ").append(label).append("：").append(value.trim()).append('\n');
    }

    private void appendFields(StringBuilder prompt, List<AiCodeFieldBo> fields) {
        if (CollUtils.isEmpty(fields)) {
            prompt.append("- 未提供字段，请根据业务说明自行设计最小字段集合。").append('\n');
            return;
        }
        int index = 1;
        for (AiCodeFieldBo field : fields) {
            if (field == null || isBlank(field.getName())) {
                continue;
            }
            prompt.append(index++).append(". ");
            prompt.append(field.getName().trim());
            appendOptionalPart(prompt, field.getTitle());
            appendOptionalPart(prompt, field.getJavaType());
            appendBooleanPart(prompt, "必填", field.getRequired());
            appendBooleanPart(prompt, "查询", field.getSearchable());
            appendBooleanPart(prompt, "列表", field.getListVisible());
            prompt.append('\n');
        }
        if (1 == index) {
            prompt.append("- 未提供有效字段，请根据业务说明自行设计最小字段集合。").append('\n');
        }
    }

    private void appendOptionalPart(StringBuilder prompt, String value) {
        if (!isBlank(value)) {
            prompt.append(" / ").append(value.trim());
        }
    }

    private void appendBooleanPart(StringBuilder prompt, String label, Boolean value) {
        if (value != null) {
            prompt.append(" / ").append(label).append("=").append(Boolean.TRUE.equals(value) ? "是" : "否");
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
