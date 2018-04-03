package org.pkaq.web.sys.dict.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseActiveEntity;

import javax.validation.constraints.NotBlank;

/**
 * 字典管理主表
 * @author S.PKAQ
 */
@Data
@Alias("dictItem")
@TableName("sys_dict_item")
@EqualsAndHashCode(callSuper = true)
public class DictItemEntity extends BaseActiveEntity<DictItemEntity> {
    /** 主表ID **/
    @NotBlank(message = "主表ID不允许为空")
    private String mainId;
    /** 字典项键 **/
    @NotBlank(message = "字典项不允许为空")
    private String keyName;
    /** 字典项值 **/
    @NotBlank(message = "字典值不允许为空")
    private String keyValue;
    /** 输出排序 **/
    private Integer orders;
    /** 是否启用 **/
    private String status;

    private static final long serialVersionUID = 1L;

}