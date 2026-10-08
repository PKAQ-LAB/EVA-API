package org.pkaq.core.auth.adapter.mybatis.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.pkaq.core.auth.spi.model.AccountProfileSnapshot;

/**
 * 可选管理资料只读查询，与账号凭据查询隔离。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Mapper
public interface AuthAccountProfileMapper {
    /**
     * 获取有效账号对应的可选管理资料。
     * @param accountId 账号编号
     * @return 可选资料
     */
    @Select("""
            SELECT profile.ACCOUNT_ID, profile.CODE, profile.NAME, profile.DEPT_ID
            FROM SYS_ACCOUNT_PROFILE profile
            JOIN SYS_ACCOUNT account ON account.ID = profile.ACCOUNT_ID
            WHERE profile.ACCOUNT_ID = #{accountId} AND COALESCE(account.DELETED, 0) = 0
            """)
    AccountProfileSnapshot findProfile(Long accountId);
}
