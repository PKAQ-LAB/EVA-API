package org.pkaq.web.jxc.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.PureBaseEntity;

/**
 * 商品管理
 * @author PKAQ
 */
@Data
@Alias("goods")
@ApiModel
@TableName("t_jxc_goods")
@EqualsAndHashCode(callSuper = true)
public class GoodsEntity extends PureBaseEntity {
    @ApiModelProperty(value = "品名")
    private String name;

    @ApiModelProperty("品类")
    private String category;

    @ApiModelProperty("型号")
    private String model;

    @ApiModelProperty("助记码")
    private String mnemonic;

    @ApiModelProperty("二维码")
    private String barcode;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("装箱规格")
    private Integer boxunit;

    @ApiModelProperty("生产厂家")
    private String productor;
}