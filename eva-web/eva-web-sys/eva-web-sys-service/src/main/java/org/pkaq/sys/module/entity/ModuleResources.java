package org.pkaq.sys.module.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdLineEntity;

/**
 * 模块管理
 *
 * @author : PKAQ
 */
@Data
@Alias("moduleResource")
@TableName("sys_module_resources")
@EqualsAndHashCode(callSuper = false)
public class ModuleResources extends StdLineEntity {

    private String code;

    private String resourceDesc;

    private String resourceUrl;

    private String resourceType;

    private Double sort;

    /** 是否已授权，仅用于角色资源查询。 */
    @TableField(exist = false)
    private Integer checked;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private long deleted;
}
