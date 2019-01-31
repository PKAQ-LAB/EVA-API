package io.nerv.web.jxc.purchasing.orders.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import io.nerv.core.mvc.entity.PureBaseEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 采购入库单
 * @author PKAQ
 */
@Data
@Alias("instock")
@ApiModel("采购入库单")
@TableName("jxc_purchasing_order")
@EqualsAndHashCode(callSuper = true)
public class PurchasingOrderEntity extends PureBaseEntity {

    @ApiModelProperty(value = "入库单号")
    @TableField("CODE")
    private String code;

    @ApiModelProperty(value = "入库日期")
    @TableField("ORDER_DATE")
    private LocalDate orderDate;

    @ApiModelProperty(value = "仓库")
    @TableField("STOCK")
    private String stock;

    @ApiModelProperty(value = "采购类型")
    @TableField("PURCHASING_TYPE")
    private String purchasingType;

    @ApiModelProperty(value = "制单人")
    @TableField("OPERATOR")
    private String operator;

    @ApiModelProperty(value = "制单人名称")
    @TableField("OPERATOR_NM")
    private String operatorNm;

    @ApiModelProperty(value = "采购人")
    @TableField("PURCHASER")
    private String purchaser;

    @ApiModelProperty(value = "采购人名称")
    @TableField("PURCHASER_NM")
    private String purchaserNm;

    @ApiModelProperty(value = "供应商")
    @TableField("SUPPLIER_ID")
    private String supplierId;

    @ApiModelProperty(value = "供应商名称")
    @TableField("SUPPLIER_NM")
    private String supplierNm;

    /** 子表数据 **/
    @TableField(exist = false)
    private List<PurchasingOrderLineEntity> line;
}