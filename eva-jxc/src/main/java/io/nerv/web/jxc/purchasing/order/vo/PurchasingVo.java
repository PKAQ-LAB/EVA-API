package io.nerv.web.jxc.purchasing.order.vo;

import io.swagger.annotations.ApiModel;

import java.time.LocalDateTime;

@ApiModel("采购入库单列表vo")
public interface PurchasingVo {
    String getId();
    String getCode();
    LocalDateTime getOrderDate();
    String getStock();
    String getPurchasingType();
    String getPurchaserNm();
    String getSupplierNm();
    String getName();
    String getCategory();
}
