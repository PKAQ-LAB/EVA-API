package org.pkaq.sys.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 字典视图
 *
 * @author: S.PKAQ
 */
@Data
@Alias("vdict")
@TableName("V_DICT")
public class DictViewEntity {

    private String code;

    private String name;

    private String dCode;

    private String dValue;

    private String frozen;

    private int sort;
}
