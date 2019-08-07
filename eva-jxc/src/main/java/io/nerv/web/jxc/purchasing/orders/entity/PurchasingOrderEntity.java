package io.nerv.web.jxc.purchasing.orders.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 采购入库单
 * @author PKAQ
 */
@Data
@Alias("jxc_purchasing")
@ApiModel("采购入库单")
@TableName("jxc_purchasing_order")
@EqualsAndHashCode(callSuper = true)
public class PurchasingOrderEntity extends StdBaseEntity {

    @ApiModelProperty(value = "入库单号")
    private String code;

    @ApiModelProperty(value = "入库日期")
    private LocalDate orderDate;

    @ApiModelProperty(value = "仓库")
    private String stock;

    @ApiModelProperty(value = "采购类型")
    private String purchasingType;

    @ApiModelProperty(value = "制单人")
    private String operator;

    @ApiModelProperty(value = "制单人名称")
    private String operatorNm;

    @ApiModelProperty(value = "采购人")
    private String purchaser;

    @ApiModelProperty(value = "采购人名称")
    private String purchaserNm;

    @ApiModelProperty(value = "供应商")
    private String supplierId;

    @ApiModelProperty(value = "供应商名称")
    private String supplierNm;

    /** 子表数据 **/
    @TableField(exist = false)
    private List<PurchasingOrderLineEntity> line;
}