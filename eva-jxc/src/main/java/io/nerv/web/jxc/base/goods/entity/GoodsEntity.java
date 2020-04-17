package io.nerv.web.jxc.base.goods.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * 商品管理
 * @author PKAQ
 */
@Data
@Alias("goods")
@ApiModel
@TableName("JXC_BASE_GOODS")
@EqualsAndHashCode(callSuper = true)
public class GoodsEntity extends StdBaseEntity {
    @ApiModelProperty(value = "品名")
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    @ApiModelProperty("品类")
    private String category;

    @ApiModelProperty("图片")
    private String avatar;

    @ApiModelProperty("货号")
    @TableField(condition = SqlCondition.LIKE)
    private String itemNo;

    @ApiModelProperty("助记码")
    private String mnemonic;

    @ApiModelProperty("条码")
    private String barcode;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("装箱数量")
    private Integer boxunit;

    @ApiModelProperty("生产厂家")
    private String factory;
}