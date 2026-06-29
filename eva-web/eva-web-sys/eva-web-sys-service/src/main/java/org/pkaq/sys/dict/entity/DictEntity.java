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
 * 字典节点实体
 *
 * @author S.PKAQ
 */
@Data
@Alias("dict")
@TableName("SYS_DICT")
@EqualsAndHashCode(callSuper = true)
public class DictEntity extends StdEntity implements Serializable {

    /** 字典类型编码 */
    private String type;

    /** 节点编码 */
    private String code;

    /** 节点名称 */
    private String name;

    /** 叶子节点提交值 */
    private String value;

    /** 上级节点ID，根节点为0 */
    private Long pid;

    /** 树路径，格式为 /id 或 /parent/id */
    private String path;

    /** 是否叶子节点 */
    private Boolean isleaf;

    /** 子节点列表 */
    @TableField(exist = false)
    private List<DictEntity> children;
}
