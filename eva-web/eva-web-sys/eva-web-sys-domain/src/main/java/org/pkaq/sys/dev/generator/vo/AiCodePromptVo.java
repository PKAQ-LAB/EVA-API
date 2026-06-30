package org.pkaq.sys.dev.generator.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

/**
 * AI代码生成提示词视图。
 *
 * @author PKAQ
 */
@Data
@Schema(title = "AI代码生成提示词视图")
public class AiCodePromptVo implements Vo {

    @Schema(description = "提示词标题")
    private String title;

    @Schema(description = "可直接提交给AI的完整提示词")
    private String prompt;
}
