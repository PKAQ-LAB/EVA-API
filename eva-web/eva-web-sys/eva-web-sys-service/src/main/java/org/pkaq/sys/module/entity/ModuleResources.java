package org.pkaq.sys.module.entity;

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

    private String resourceDesc;

    private String resourceUrl;

    private String resourceType;
}