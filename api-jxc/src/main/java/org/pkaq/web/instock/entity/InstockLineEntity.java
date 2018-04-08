package org.pkaq.web.instock.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseActiveEntity;
import org.pkaq.core.mvc.entity.BaseEntity;
import org.pkaq.core.mvc.entity.BaseLineActiveEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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