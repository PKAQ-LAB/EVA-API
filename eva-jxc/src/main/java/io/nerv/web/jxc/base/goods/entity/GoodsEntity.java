package io.nerv.web.jxc.base.goods.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;

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

    @ApiModelProperty("型号")
    @TableField(condition = SqlCondition.LIKE)
    private String model;

    @ApiModelProperty("助记码")
    private String mnemonic;

    @ApiModelProperty("条码")
    private String barcode;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("装箱规格")
    private Integer boxunit;
}