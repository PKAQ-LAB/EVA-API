package io.nerv.web.jxc.sale.slip.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;

@Data
@ApiModel("零售单")
@Alias("saleSlip")
@TableName("JXC_SALES_SLIP")
@EqualsAndHashCode(callSuper = true)
public class SlipEntity extends StdBaseEntity {

  @ApiModelProperty(value = "商品ID")
  private String  goodsId;

  @ApiModelProperty(value = "商品名称")
  @TableField(condition = SqlCondition.LIKE)
  private String  goodsName;

  @ApiModelProperty(value = "货号")
  private String  itemNo;

  @ApiModelProperty(value = "订单号")
  private String  orderCode;

  @ApiModelProperty(value = "来源平台")
  private String  sourcePlatform;

  @ApiModelProperty(value = "下单时间")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private String  dealTime;

  @ApiModelProperty(value = "下单数量")
  private Double  nummer;

  @ApiModelProperty(value = "下单价格")
  private BigDecimal  price;

  @ApiModelProperty(value = "总成交额")
  private BigDecimal totalPrice;

  @ApiModelProperty(value = "成本价")
  private BigDecimal costPrice;

  @ApiModelProperty(value = "总成本")
  private BigDecimal totalCost;

  @ApiModelProperty(value = "利润")
  private BigDecimal  profit;

  @ApiModelProperty(value = "收货人")
  private String  receiver;

  @ApiModelProperty(value = "收货地址")
  private String  receiverAddr;

  @ApiModelProperty(value = "收货人手机")
  private String  receiverPhone;

  @ApiModelProperty(value = "快递费用")
  private BigDecimal  shipPrice;

  @ApiModelProperty(value = "快递单号")
  private String  shipNumber;

  @ApiModelProperty(value = "快递公司")
  private String  shipCompany;

  @ApiModelProperty(value = "供应商姓名")
  private String  supplierName;

  @ApiModelProperty(value = "供应商货号")
  private String  supplierNo;

  @ApiModelProperty(value = "供应商手机")
  private String  supplierPhone;
}
