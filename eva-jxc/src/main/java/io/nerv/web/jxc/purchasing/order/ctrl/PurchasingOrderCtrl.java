package io.nerv.web.jxc.purchasing.order.ctrl;

import io.nerv.core.constant.PageConstant;
import io.nerv.core.mvc.ctrl.jpa.StdBaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrder;
import io.nerv.web.jxc.purchasing.order.service.PurchasingServices;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 8:00
 */
@Api( tags = "采购入库单")
@RestController
@RequestMapping("/pdos/purchasing")
public class PurchasingOrderCtrl extends StdBaseCtrl<PurchasingServices, PurchasingOrder> {
    @GetMapping("/checkCode")
    @ApiOperation(value = "校验code唯一性",response = Response.class)
    public Response checkCode(@ApiParam(name ="code", value = "要进行校验的参数", required = true)
                                String code,
                                @ApiParam(name ="id", value = "要进行校验的参数ID")
                                @RequestParam(required = false)
                                String id){
        boolean exist = this.service.checkCode(code, id);
        return exist? success("采购单号 ["+code+"] 已存在， 请重新输入", null): success();
    }

    @Override
    @GetMapping("list")
    @ApiOperation(value = "列表查询",response = Response.class)
    public Response list(@ApiParam(name ="condition", value = "模型对象")
                         PurchasingOrder domain,
                         Integer pageNo,
                         Integer pageSize){
        pageNo = pageNo == null? 0: pageNo - 1;
        pageSize = pageSize == null? PageConstant.PAGE_SIZE: pageSize;
        return this.success(this.service.listAll(domain, pageNo, pageSize));
    }
}
