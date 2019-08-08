package io.nerv.web.jxc.purchasing.order.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 采购入库明细
 * @author PKAQ
 */
@Getter
@Setter
@Entity
@ApiModel("采购入库明细")
@Table(name = "jxc_porder_line")
public class PurchasingOrderLine {
    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "io.nerv.core.util.jpa.SnowflakeGenerator")
    @Id
    private String lineId;

    @ApiModelProperty(value = "主键ID")
    @Column(name = "MAIN_ID")
    private String mainId;

    @ApiModelProperty(value = "商品ID")
    private String goodsId;

    @ApiModelProperty(value = "品名")
    private String name;

    @ApiModelProperty(value = "品类")
    private String category;

    @ApiModelProperty(value = "产品型号")
    private String model;

    @ApiModelProperty(value = "条码")
    private String barcode;

    @ApiModelProperty(value = "规格单位")
    private String unit;

    @ApiModelProperty(value = "助记码（拼音）")
    private String mnemonic;

    @ApiModelProperty(value = "进货数量")
    private BigDecimal num;

    @ApiModelProperty(value = "含税单价")
    private BigDecimal vat;

    @ApiModelProperty(value = "含税金额")
    private BigDecimal vatAmount;

    @ApiModelProperty(value = "税率")
    private BigDecimal taxRate;

    @ApiModelProperty(value = "税额")
    private BigDecimal taxAmount;

    @ApiModelProperty(value = "不含税单价")
    private BigDecimal notaxValue;

    @ApiModelProperty(value = "不含税金额")
    private BigDecimal notaxAmount;

    @ApiModelProperty(value = "库存数量")
    private BigDecimal stockNum;

    @ApiModelProperty(value = "库存金额")
    private BigDecimal stockAmount;
}