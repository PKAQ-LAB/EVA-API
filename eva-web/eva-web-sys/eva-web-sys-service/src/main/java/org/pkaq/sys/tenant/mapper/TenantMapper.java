package org.pkaq.sys.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.core.mvc.vo.SingleArray;
import org.pkaq.sys.tenant.entity.TenantEntity;
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
     * 锁定超出数量的用户
     *
     * @param tid   租户id
     * @param count 授权数量
     */
    void reGrantUser(@Param("tid") Long tid, @Param("count") int count, @Param("frz") int frz);

    /**
     * 冻结已被冻结的租户对应的用户
     * @param ids
     */
    void frozenUser(SingleArray<Long> ids);

    void unfronzenUser(SingleArray<Long> ids);
}
