package org.pkaq.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.io.Serializable;

/**
 * 字典明细实体
 *
 * @author PKAQ
 */
@Data
@Alias("dictItem")
@TableName("SYS_DICT_ITEM")
@EqualsAndHashCode(callSuper = true)
public class DictItemEntity extends StdEntity implements Serializable {

    /** 字典主表 ID */
    private Long mainId;

    /** 字典提交值 */
    private String dCode;

    /** 字典显示文本 */
    private String dValue;
}
