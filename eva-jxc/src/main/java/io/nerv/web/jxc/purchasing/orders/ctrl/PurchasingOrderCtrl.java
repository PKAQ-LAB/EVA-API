package io.nerv.web.jxc.purchasing.orders.ctrl;

import io.nerv.core.mvc.ctrl.PureBaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.web.jxc.purchasing.orders.entity.PurchasingOrderEntity;
import io.nerv.web.jxc.purchasing.orders.service.PurchasingService;
import io.nerv.web.jxc.purchasing.orders.vo.PurchasingOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("edit")
    @ApiOperation(value = "新增/编辑采购入库单",response = Response.class)
    public Response save(@ApiParam(name ="instock", value = "采购入库单对象")
                         @RequestBody PurchasingOrderEntity instock){
        this.service.save(instock);
        return success();
    }

    @GetMapping("list")
    @ApiOperation(value = "采购入库单列表查询",response = Response.class)
    public Response list(@ApiParam(name ="instock", value = "入库单查询对象")
                                 PurchasingOrderVO entity, Integer pageNo){
        return this.success(this.service.listPage(entity, pageNo));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获得采购入库单信息", response = Response.class)
    public Response getRole(@ApiParam(name = "id", value = "记录ID")
                            @PathVariable("id") String id){
        return this.success(this.service.getById(id));
    }
}
