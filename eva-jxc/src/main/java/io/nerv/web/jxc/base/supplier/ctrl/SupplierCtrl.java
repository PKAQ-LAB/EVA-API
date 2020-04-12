package io.nerv.web.jxc.base.supplier.ctrl;

import io.nerv.core.mvc.ctrl.mybatis.StdBaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.web.jxc.base.supplier.entity.SupplierEntity;
import io.nerv.web.jxc.base.supplier.service.SupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商管理
 * @author: S.PKAQ
 */
@Api(tags = "供应商管理")
@RestController
@RequestMapping("/pdos/base/supplier")
public class SupplierCtrl extends StdBaseCtrl<SupplierService, SupplierEntity> {

}
