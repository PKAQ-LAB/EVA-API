package org.pkaq.web.sys.dict.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.BaseEntity;

import java.io.Serializable;
import java.util.Date;
/**
 * 字典项管理实体类
 * @author S.PKAQ
 */
@Data
@Alias("dict")
@TableName("sys_dict")
@EqualsAndHashCode(callSuper = true)
public class DictEntity extends BaseEntity {
    // 编码
    private String code;
    // 编码描述
    private String name;
    // 上级节点
    private String parent;

    private static final long serialVersionUID = 1L;
}