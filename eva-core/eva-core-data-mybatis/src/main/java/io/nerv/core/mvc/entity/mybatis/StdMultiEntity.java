package io.nerv.core.mvc.entity.mybatis;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 实体类基类，用于存放公共属性
 * @author PKAQ
 */
@Data
public abstract class StdMultiEntity<T> extends StdEntity {

    @TableField(exist = false)
    @Schema(description = "子表")
    private List<T> lines;
}
