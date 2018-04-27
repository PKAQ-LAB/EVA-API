package org.pkaq.sys.instock.ctrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.mvc.ctrl.BaseActiveCtrl;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.sys.instock.entity.InstockEntity;
import org.pkaq.sys.instock.service.InstockService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 8:00
 */
@Api( description = "采购入库单")
@RestController
@RequestMapping("/instock")
public class InstockCtrl extends BaseActiveCtrl<InstockService> {

    @PostMapping("/save")
    @ApiOperation(value = "新增/编辑采购入库单",response = Response.class)
    public Response save(@ApiParam(name ="instock", value = "采购入库单对象")
                         @RequestBody InstockEntity instock){
        System.out.println(instock.getLine().size()+ " :::: ---");
        this.service.save(instock);
        return success();
    }
}
