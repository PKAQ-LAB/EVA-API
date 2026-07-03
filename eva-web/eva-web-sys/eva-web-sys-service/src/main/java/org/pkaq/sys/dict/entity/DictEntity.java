package org.pkaq.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.io.Serializable;

/**
 * 字典主表实体
 *
 * @author S.PKAQ
 */
@Data
@Alias("dict")
@TableName("SYS_DICT")
@EqualsAndHashCode(callSuper = true)
public class DictEntity extends StdEntity implements Serializable {

    /** 字典类型编码，单级字典模式下与 code 保持一致 */
    private String type;

    /** 字典编码 */
    private String code;

    /** 字典名称 */
    private String name;
}
