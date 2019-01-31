package io.nerv.web.jxc.purchasing.orders.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;
import io.nerv.core.mvc.entity.PureBaseEntity;

import java.math.BigDecimal;

/**
 * 采购入库明细
 * @author PKAQ
 */
@Data
@Alias("instockLine")
@ApiModel("采购入库明细")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("jxc_purchasing_order_line")
public class PurchasingOrderLineEntity extends PureBaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField("MAIN_ID")
    private String mainId;

    @ApiModelProperty(value = "商品ID")
    @TableField("GOODS_ID")
    private String goodsId;

    @ApiModelProperty(value = "品名")
    @TableField("NAME")
    private String name;

    @ApiModelProperty(value = "品类")
    @TableField("CATEGORY")
    private String category;

    @ApiModelProperty(value = "产品型号")
    @TableField("MODEL")
    private String model;

    @ApiModelProperty(value = "条码")
    @TableField("BARCODE")
    private String barcode;

    @ApiModelProperty(value = "规格单位")
    @TableField("UNIT")
    private String unit;

    @ApiModelProperty(value = "助记码（拼音）")
    @TableField("MNEMONIC")
    private String mnemonic;

    @ApiModelProperty(value = "进货数量")
    @TableField("NUM")
    private BigDecimal num;

    @ApiModelProperty(value = "含税单价")
    @TableField("VAT")
    private BigDecimal vat;

    @ApiModelProperty(value = "含税金额")
    @TableField("VAT_AMOUNT")
    private BigDecimal vatAmount;

    @ApiModelProperty(value = "税率")
    @TableField("TAX_RATE")
    private BigDecimal taxRate;

    @ApiModelProperty(value = "税额")
    @TableField("TAX_AMOUNT")
    private BigDecimal taxAmount;

    @ApiModelProperty(value = "不含税单价")
    @TableField("NOTAX_VALUE")
    private BigDecimal notaxValue;

    @ApiModelProperty(value = "不含税金额")
    @TableField("NOTAX_AMOUNT")
    private BigDecimal notaxAmount;
}