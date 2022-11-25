package io.nerv.biz.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 字典视图
 *
 * @author: S.PKAQ
 */
@Data
@Alias("dictView")
@TableName("v_dict")
public class DictViewEntity {
    @Schema(description = "字典编码")
    private String code;

    @Schema(description = "字典描述")
    private String name;

    @Schema(description = "字典项key")
    private String keyName;

    @Schema(description = "字典项value")
    private String keyValue;

    @Schema(description = "字典项排序")
    private String orders;
}
