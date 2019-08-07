package io.nerv.web.jxc.inventory.accbook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;

import java.math.BigDecimal;

/**
 * 库存台帐entity
 * @author: S.PKAQ
 * @Datetime: 2018/10/14 20:05
 */

@Data
@Alias("stock")
@TableName("t_jxc_stock")
@EqualsAndHashCode(callSuper = true)
@ApiModel("采购入库单")
public class StockEntity extends StdBaseEntity {
 @ApiModelProperty("商品ID")
 private String goodsId;

 @ApiModelProperty("商品助记码")
 private String mnemonic;

 @ApiModelProperty("商品条码")
 private String barcode;

 @ApiModelProperty("商品型号")
 private String model;

 @ApiModelProperty("商品名称")
 private String name;

 @ApiModelProperty("商品分类")
 private String category;

 @ApiModelProperty("单位")
 private String unit;

 @ApiModelProperty("单价")
 private BigDecimal unitPrice;

 @ApiModelProperty("仓库ID")
 private String storageId;

 @ApiModelProperty("货位")
 private String allocation;

 @ApiModelProperty("库存数量")
 private Integer onhand;

 @ApiModelProperty("可用数量")
 private Integer available;

 @ApiModelProperty("库存金额")
 private BigDecimal amount;

 @ApiModelProperty("盘点日期")
 private String lastCheck;

 @ApiModelProperty("盘点数量")
 private Integer stocktakingQuantity;

 @ApiModelProperty("库存状态")
 private String state;
}
