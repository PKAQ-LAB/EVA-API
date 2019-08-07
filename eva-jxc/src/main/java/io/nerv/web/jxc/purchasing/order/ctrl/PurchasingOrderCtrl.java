package io.nerv.web.jxc.purchasing.order.ctrl;

import io.nerv.core.mvc.ctrl.jpa.StdBaseCtrl;
import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrder;
import io.nerv.web.jxc.purchasing.order.service.PurchasingServices;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 8:00
 */
@Api( tags = "采购入库单")
@RestController
@RequestMapping("purchasing")
public class PurchasingOrderCtrl extends StdBaseCtrl<PurchasingServices, PurchasingOrder> {

}
