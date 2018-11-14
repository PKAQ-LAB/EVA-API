package org.pkaq.web.jxc.stock.ctrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.core.mvc.ctrl.PureBaseCtrl;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.web.jxc.stock.entity.StockEntity;
import org.pkaq.web.jxc.stock.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 库存管理controller
 * @author: S.PKAQ
 * @Datetime: 2018/10/14 20:01
 */
@Api( description = "库存台帐管理")
@RestController
@RequestMapping("stock")
public class StockCtrl extends PureBaseCtrl<StockService> {
    @GetMapping("list")
    @ApiOperation(value = "库存台帐列表",response = Response.class)
    public Response list(@ApiParam(name ="goods", value = "库存查询对象")
                                 StockEntity entity, Integer pageNo){
        return this.success(this.service.listPage(entity, pageNo));
    }

    @PostMapping("lock")
    @ApiOperation(value = "锁定库存",response = Response.class)
    public Response lock(){
        this.service.lock();
        return this.success();
    }

    @PostMapping("unlock")
    @ApiOperation(value = "解锁库存",response = Response.class)
    public Response unlock(){
        this.service.unlock();
        return this.success();
    }
}
