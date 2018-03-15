package org.pkaq.web.sys.dict.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 字典管理主表
 * @author S.PKAQ
 */
@Data
@Alias("dictItem")
@TableName("sys_dict_item")
@EqualsAndHashCode(callSuper = true)
public class DictItemEntity extends BaseEntity{
    /** 主表ID **/
    private String mainId;
    /** 字典项键 **/
    private String keyName;
    /** 字典项值 **/
    private String keyValue;
    /** 输出排序 **/
    private Integer orders;
    /** 是否启用 **/
    private String status;

    private static final long serialVersionUID = 1L;
}