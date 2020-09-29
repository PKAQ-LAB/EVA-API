package io.nerv.web.jxc.base.goods.ctrl;

import io.nerv.core.mvc.ctrl.mybatis.StdBaseCtrl;
import io.nerv.core.mvc.vo.Response;
import io.nerv.web.jxc.base.goods.entity.GoodsEntity;
import io.nerv.web.jxc.base.goods.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品管理
 * @author: S.PKAQ
 */
@Api(tags = "产品管理")
@RestController
@RequestMapping("/pdos/base/goods")
public class GoodsCtrl extends StdBaseCtrl<GoodsService, GoodsEntity> {

    @PostMapping("checkUnique")
    @ApiOperation(value = "校验barcode唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="goods", value = "要进行校验的参数")
                                @RequestBody GoodsEntity goodsEntity){
        boolean exist = this.service.checkUnique(goodsEntity);
        return exist? failure(): success();
    }
}
