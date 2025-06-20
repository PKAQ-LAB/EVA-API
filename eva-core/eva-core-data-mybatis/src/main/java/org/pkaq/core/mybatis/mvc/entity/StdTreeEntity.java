package org.pkaq.core.mybatis.mvc.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 树形结构实体基类
 *
 * @author: S.PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StdTreeEntity extends StdEntity {

    /** id **/
    private String id;

    @TableField(condition = SqlCondition.LIKE)
    /** 编码 **/
    private String code;

    /** 节点名称 **/
    private String name;

    /** 上级节点id **/
    private String pid;

    /** 路径 **/
    private String path;

    /** 是否叶子 **/
    private Boolean isleaf;
}
