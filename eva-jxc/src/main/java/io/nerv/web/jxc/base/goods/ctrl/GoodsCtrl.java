package io.nerv.web.jxc.base.goods.ctrl;

import io.nerv.web.jxc.base.goods.entity.GoodsEntity;
import io.nerv.web.jxc.base.goods.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.nerv.core.mvc.ctrl.StdBaseCtrl;
import io.nerv.core.mvc.util.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 8:00
 */
@Api( description = "商品管理",tags = "进销存")
@RestController
@RequestMapping("jxc/goods")
public class GoodsCtrl extends StdBaseCtrl<GoodsService, GoodsEntity> {

    @PostMapping("checkUnique")
    @ApiOperation(value = "校验barcode唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="goods", value = "要进行校验的参数")
                                @RequestBody GoodsEntity goodsEntity){
        boolean exist = this.service.checkUnique(goodsEntity);
        return exist? failure(): success();
    }
}
