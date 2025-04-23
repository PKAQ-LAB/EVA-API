package org.pkaq.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.io.Serializable;
import java.util.List;

/**
 * 字典项管理实体类
 *
 * @author S.PKAQ
 */
@Data
@Alias("dict")
@TableName("SYS_DICT")
@EqualsAndHashCode(callSuper = true)
public class DictEntity extends StdEntity implements Serializable {

    /** 字典分类编码 **/
    private String code;

    /** 字典分类名称 **/
    private String name;

    private String pid;

    /** 树路径(id) **/
    private String path;

    /** 字典项列表 **/
    @TableField(exist = false)
    private List<DictItemEntity> lines;

    /** 子节点 **/
    @TableField(exist = false)
    private List<DictEntity> children;

}