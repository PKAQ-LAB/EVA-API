package io.nerv.generator.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 表信息，关联到当前字段信息
 */
@Data
@Accessors(chain = true)
public class TableConfig {
    // 表名o
    private String name;
    // 表注释
    private String comment;
    // 表字段信息
    private List<TableField> fields;
}
