package io.nerv.generator.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 表信息，关联到当前字段信息
 */
@Data
@Accessors(chain = true)
public class TableConfig {
    // 导入的包信息
    private final Set<String> importPackages = new HashSet<>();
    // 表名
    private String name;
    // 表注释
    private String comment;
    // 表字段信息
    private List<TableField> fields;
}
