package org.pkaq.web.jxc.sale.ctrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.core.mvc.util.SingleArray;
import org.pkaq.web.jxc.sale.service.SaleService;
import org.pkaq.web.jxc.sale.entity.SaleEntity;
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
public class SaleCtrl extends BaseCtrl<SaleService> {

    @PostMapping("/checkOut")
    @ApiOperation(value = "零售开单-结帐收银",response = Response.class)
    public Response checkOut(@ApiParam(name ="invoices", value = "零售单 商品对象数组")
                             @RequestBody() SingleArray<SaleEntity> param){
        this.service.checkOut(param.getParam());
        return success();
    }
}
