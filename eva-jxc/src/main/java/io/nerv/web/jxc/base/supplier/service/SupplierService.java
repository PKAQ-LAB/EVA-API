package io.nerv.web.jxc.base.supplier.service;

import io.nerv.core.mvc.service.mybatis.StdBaseService;
import io.nerv.web.jxc.base.supplier.entity.SupplierEntity;
import io.nerv.web.jxc.base.supplier.mapper.SupplierMapper;
import org.springframework.stereotype.Service;

/**
 * 供应商管理
 * @author: S.PKAQ
 */
@Service
public class SupplierService extends StdBaseService<SupplierMapper, SupplierEntity> {
}
