package org.pkaq.sys.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.springframework.stereotype.Repository;

/**
 * 租户管理 Mapper
 * <p>
 * 注：原先的 frozenUser / unfronzenUser / reGrantUser 自定义方法已移除，
 * 这些方法当时无对应 XML 实现，调用会抛 BindingException。
 * 现在 Service 用 LambdaUpdateWrapper 直接表达，更安全也更易调试。
 *
 * @author PKAQ
 */
@Mapper
@Repository
@Ignore
public interface TenantMapper extends BaseMapper<TenantEntity> {
}
