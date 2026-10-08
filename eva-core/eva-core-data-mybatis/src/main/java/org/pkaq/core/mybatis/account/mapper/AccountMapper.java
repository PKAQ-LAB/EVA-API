package org.pkaq.core.mybatis.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.core.mybatis.account.entity.AccountEntity;

/**
 * 账号存储操作。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Mapper
public interface AccountMapper extends BaseMapper<AccountEntity> {
    /** 唯一性覆盖当前租户全部有效账号，不受管理端数据可见范围缩小。 */
    @Ignore
    @Select("SELECT COUNT(*) FROM SYS_ACCOUNT WHERE ACCOUNT = #{account} AND COALESCE(DELETED, 0) = 0")
    Long countActiveAccount(@Param("account") String account);
}
