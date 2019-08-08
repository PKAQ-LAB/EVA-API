package io.nerv.web.jxc.purchasing.order.domain;

import io.nerv.core.mvc.entity.jpa.StdBaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购入库单
 * @author PKAQ
 */
@Data
@Entity
@ApiModel("采购入库单")
@Table(name = "JXC_PURCHASING_ORDER")
@EqualsAndHashCode(callSuper = true)
public class PurchasingOrder extends StdBaseDomain {

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

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, orphanRemoval = true, fetch=FetchType.LAZY)
    @JoinColumn(name = "main_id")
    private List<PurchasingOrderLine> line;
}