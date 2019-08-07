package io.nerv.web.jxc.purchasing.orders.ctrl;

import io.nerv.core.mvc.ctrl.mybatis.PureBaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.web.jxc.purchasing.orders.service.PurchasingService;
import io.nerv.web.jxc.purchasing.orders.vo.PurchasingOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 8:00
 */
@Api( tags = "采购入库单")
@RestController
@RequestMapping("instock")
public class PurchasingOrderCtrl extends PureBaseCtrl<PurchasingService> {


    @GetMapping("list")
    @ApiOperation(value = "采购入库单列表查询",response = Response.class)
    public Response list(@ApiParam(name ="instock", value = "入库单查询对象")
                                 PurchasingOrderVO entity, Integer pageNo){
        return this.success(this.service.listPage(entity, pageNo));
    }

}
