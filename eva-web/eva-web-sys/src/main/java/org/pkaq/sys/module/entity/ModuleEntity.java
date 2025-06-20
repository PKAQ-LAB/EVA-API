package org.pkaq.sys.module.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdTreeEntity;

/**
 * 模块管理
 *
 * @author : PKAQ
 */
@Data
@Alias("module")
@TableName("sys_module")
@EqualsAndHashCode(callSuper = true)
public class ModuleEntity extends StdTreeEntity {

    /** 模块图标 **/
    private String icon;

    /** 模块路由url **/
    private String routeurl;

    /** 模块前端model url **/
    private String modelurl;
}