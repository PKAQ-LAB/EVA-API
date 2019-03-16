package io.nerv.web.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 字典视图
 * @author: S.PKAQ
 * @Datetime: 2019/3/15 15:09
 */
@Data
@Alias("dictView")
@TableName("v_dict")
public class DictViewEntity {
    @ApiModelProperty("字典编码")
    private String code;

    @ApiModelProperty("字典描述")
    private String name;

    @ApiModelProperty("字典项key")
    private String keyName;

    @ApiModelProperty("字典项value")
    private String keyValue;

    @ApiModelProperty("字典项排序")
    private String orders;
}
