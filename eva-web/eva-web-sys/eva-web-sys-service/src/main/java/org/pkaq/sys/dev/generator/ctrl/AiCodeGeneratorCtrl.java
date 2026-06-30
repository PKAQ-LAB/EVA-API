package org.pkaq.sys.dev.generator.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.dev.generator.bo.AiCodePromptBo;
import org.pkaq.sys.dev.generator.service.AiCodeGeneratorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI代码生成提示词控制器。
 *
 * @author PKAQ
 */
@Tag(name = "AI代码生成")
@RestController
@RequestMapping("/dev/generator")
@RequiredArgsConstructor
public class AiCodeGeneratorCtrl extends Ctrl {

    private final AiCodeGeneratorService aiCodeGeneratorService;

    @PostMapping("/prompt")
    @Operation(summary = "生成AI代码提示词")
    @BizLog(operateType = BizLogCodes.QUERY, description = "生成了AI代码提示词[{0}]", args = {"param:0"})
    public Response<Object> prompt(@RequestBody @Valid AiCodePromptBo bo) {
        return success(this.aiCodeGeneratorService.buildPrompt(bo));
    }
}
