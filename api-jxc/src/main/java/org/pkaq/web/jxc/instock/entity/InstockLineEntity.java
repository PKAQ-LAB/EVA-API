package org.pkaq.web.jxc.instock.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseLineActiveEntity;

import java.math.BigDecimal;

/**
 * 采购入库明细
 * @author PKAQ
 */
@Data
@Alias("instockLine")
@TableName("t_instock_line")
@EqualsAndHashCode(callSuper = true)
public class InstockLineEntity extends BaseLineActiveEntity {

    private String barcode;

    private String name;

    private String category;

    private String unit;

    private Integer boxunit;

    private Integer num;

    private BigDecimal price;

    private BigDecimal subtitle;

    private String supplier;

    private String productor;

}