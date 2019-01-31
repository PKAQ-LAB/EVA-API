package io.nerv.web.jxc.sales.orders.ctrl;

import io.nerv.web.jxc.sales.orders.service.SaleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.nerv.core.mvc.ctrl.BaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.mvc.util.SingleArray;
import io.nerv.web.jxc.sale.entity.SaleEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 零售开单ctrl
 * @author: S.PKAQ
 * @Datetime: 2018/3/26 22:54
 */
@RestController
@RequestMapping("/sale")
@Api( description = "零售开单")
public class SaleCtrl extends BaseCtrl<SaleService, SaleEntity> {

    @PostMapping("/checkOut")
    @ApiOperation(value = "零售开单-结帐收银",response = Response.class)
    public Response checkOut(@ApiParam(name ="invoices", value = "零售单 商品对象数组")
                             @RequestBody() SingleArray<SaleEntity> param){
        this.service.checkOut(param.getParam());
        return success();
    }
}
