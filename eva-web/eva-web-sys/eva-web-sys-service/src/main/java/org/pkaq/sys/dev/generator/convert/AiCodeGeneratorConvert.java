package org.pkaq.sys.dev.generator.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.pkaq.core.mvc.convert.MapConvertConfig;
import org.pkaq.sys.dev.generator.vo.AiCodePromptVo;

/**
 * AI代码生成结果转换器。
 *
 * @author PKAQ
 */
@Mapper(config = MapConvertConfig.class)
public interface AiCodeGeneratorConvert {

    /**
     * 将标题和提示词转换为视图对象。
     *
     * @param title 提示词标题
     * @param prompt 完整提示词
     * @return 提示词视图对象
     */
    @Mapping(target = "title", source = "title")
    @Mapping(target = "prompt", source = "prompt")
    AiCodePromptVo toVo(String title, String prompt);
}
