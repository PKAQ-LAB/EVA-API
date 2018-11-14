package org.pkaq.web.jxc.goods.ctrl;

import io.swagger.annotations.Api;
import org.pkaq.core.mvc.ctrl.BaseCtrl;
import org.pkaq.web.jxc.goods.entity.GoodsEntity;
import org.pkaq.web.jxc.goods.service.GoodsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 8:00
 */
@Api( description = "采购入库单")
@RestController
@RequestMapping("goods")
public class GoodsCtrl extends BaseCtrl<GoodsService, GoodsEntity> {

}
