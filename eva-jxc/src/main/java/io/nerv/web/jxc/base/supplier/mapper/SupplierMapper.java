package io.nerv.web.jxc.base.supplier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.web.jxc.base.supplier.entity.SupplierEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 供应商管理
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface SupplierMapper extends BaseMapper<SupplierEntity> {
}
