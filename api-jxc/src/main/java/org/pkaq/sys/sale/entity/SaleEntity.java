package org.pkaq.sys.sale.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseEntity;

/**
 * 零售开单实体类
 * @author: S.PKAQ
 * @Datetime: 2018/3/26 22:58
 */
@Data
@Alias("sale")
@TableName("T_RETAIL_SALE")
@EqualsAndHashCode(callSuper = true)
public class SaleEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /** 商品ID **/
    private String goodsId;
    /** 订单编号 **/
    private String orderCode;
    /** 销售数量 **/
    private Double nummer;
    /** 销售单价 **/
    private Double price;
    /** 单项合计 **/
    private Double total;
}
