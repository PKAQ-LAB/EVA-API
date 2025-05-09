package org.pkaq.sys.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.core.mybatis.util.Page;
import org.pkaq.sys.tenant.bo.TenantQueryBo;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.vo.TenantListVo;
import org.springframework.stereotype.Repository;

/**
 * 租户管理
 *
 * @author PKAQ
 */
@Mapper
@Repository
@Ignore
public interface TenantMapper extends BaseMapper<TenantEntity> {
    /**
     * 分页查询租户
     */
    IPage<TenantListVo> listPage(Page<TenantListVo> pagination, @Param("q") TenantQueryBo queryBo);

    /**
     * 锁定超出数量的用户
     * @param id    租户id
     * @param count 授权数量
     */
    void lockExcessUsers(@Param("id") String id, @Param("count") int count);
}
