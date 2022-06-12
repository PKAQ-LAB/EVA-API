package io.nerv.web.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 字典视图
 * @author: S.PKAQ
 */
@Data
@Alias("dictView")
@TableName("v_dict")
public class DictViewEntity {
    @Schema(name = "字典编码")
    private String code;

    @Schema(name = "字典描述")
    private String name;

    @Schema(name = "字典项key")
    private String keyName;

    @Schema(name = "字典项value")
    private String keyValue;

    @Schema(name = "字典项排序")
    private String orders;
}
