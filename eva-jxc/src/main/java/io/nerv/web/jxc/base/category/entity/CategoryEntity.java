package io.nerv.web.jxc.base.category.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.mybatis.BaseTreeEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * @author PKAQ
 * @since 2018-11-17
 */
@Data
@ApiModel("分类编码管理")
@Alias("jxc_category")
@TableName("jxc_base_category")
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CategoryEntity extends BaseTreeEntity {

    /** 分类编码 */
    private String code;

    /** 分类名称 */
    private String name;

    /** 排序 */
    private Integer orders;

    /**  状态 */
    private String status;

}
