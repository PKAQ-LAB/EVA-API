package org.pkaq.core.mybatis.mvc.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 树形结构实体基类
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StdTreeEntity extends StdEntity {
    /**
     * 编码
     **/
    @TableField(condition = SqlCondition.LIKE)
    private String code;

    /**
     * 节点名称
     **/
    private String name;

    /**
     * 上级节点id
     **/
    private Long pid;

    /**
     * 路径
     **/
    private String path;

    /**
     * 是否叶子
     **/
    private Boolean isleaf;

    /**
     * 子节点
     **/
    private List<StdTreeEntity> children;
}
