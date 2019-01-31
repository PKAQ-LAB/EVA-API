package io.nerv.web.jxc.purchasing.orders.vo;

import io.nerv.web.jxc.purchasing.orders.entity.PurchasingOrderEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购入库单VO对象
 * @author: S.PKAQ
 * @Datetime: 2018/10/15 20:14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PurchasingOrderVO extends PurchasingOrderEntity {
    @ApiModelProperty("入库日期")
    private Date indate;

    @ApiModelProperty("入库单号")
    private String incode;

    @ApiModelProperty("购买数量")
    private Integer buynum;

    @ApiModelProperty("购买单价")
    private BigDecimal buyprice;

    @ApiModelProperty("条码")
    private String barcode;

    @ApiModelProperty("货品名称")
    private String name;

    @ApiModelProperty("货品分类")
    private String category;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("型号")
    private String model;

    @ApiModelProperty("助记码")
    private String mnemonic;

    @ApiModelProperty("供应商名称")
    private String supplier;
}
