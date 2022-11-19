package io.nerv.core.mybatis.mvc.entity.mybatis;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

/**
 * 实体类基类，用于存放公共属性
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class StdMultiEntity<T> extends StdEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @TableField(jdbcType = JdbcType.VARCHAR)
    private String id;

    @TableField(exist = false)
    @Schema(description = "子表")
    private List<T> lines;
}
