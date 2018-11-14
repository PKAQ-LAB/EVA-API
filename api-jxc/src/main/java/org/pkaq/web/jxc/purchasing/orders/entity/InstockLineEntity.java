package org.pkaq.web.jxc.instock.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.PureBaseEntity;

import java.math.BigDecimal;

/**
 * 采购入库明细
 * @author PKAQ
 */
@Data
@Alias("instockLine")
@TableName("t_jxc_instock_line")
@EqualsAndHashCode(callSuper = true)
@ApiModel("采购入库明细")
public class InstockLineEntity extends PureBaseEntity {
    @ApiModelProperty("主表ID")
    private String mainId;

    @ApiModelProperty("条码")
    private String barcode;

    @ApiModelProperty("货品名称")
    private String name;

    @ApiModelProperty("货品分类")
    private String category;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("货品ID")
    private String goodsId;

    @ApiModelProperty("购买数量")
    private Integer buynum;

    @ApiModelProperty("购买单价")
    private BigDecimal buyprice;

    @ApiModelProperty("购买金额")
    private BigDecimal buyamount;

    @ApiModelProperty("型号")
    private String model;

    @ApiModelProperty("助记码")
    private String mnemonic;

    @ApiModelProperty("供应商")
    private String supplier;
}