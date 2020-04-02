package io.nerv.web.jxc.sale.slip.ctrl;

import io.nerv.core.mvc.ctrl.mybatis.StdBaseCtrl;
import io.nerv.web.jxc.sale.slip.entity.SlipEntity;
import io.nerv.web.jxc.sale.slip.service.SlipService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 零售开单ctrl
 * @author: S.PKAQ
 * @Datetime: 2018/3/26 22:54
 */
@RestController
@RequestMapping("/sale")
@Api( tags = "线上销售单")
public class SlipCtrl extends StdBaseCtrl<SlipService, SlipEntity> {

}
