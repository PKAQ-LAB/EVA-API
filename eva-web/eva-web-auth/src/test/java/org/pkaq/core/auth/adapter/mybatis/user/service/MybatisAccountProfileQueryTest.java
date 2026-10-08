package org.pkaq.core.auth.adapter.mybatis.user.service;

import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.mybatis.user.mapper.AuthAccountProfileMapper;
import org.pkaq.core.auth.spi.model.AccountProfileSnapshot;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 可选账号管理资料查询与删除边界的隔离测试。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class MybatisAccountProfileQueryTest {
    @Test
    void optionalProfileIsReturnedWithoutAccountCredentials() {
        AuthAccountProfileMapper mapper = mock(AuthAccountProfileMapper.class);
        AccountProfileSnapshot profile = new AccountProfileSnapshot();
        profile.setAccountId(7L);
        profile.setCode("USER007");
        profile.setName("管理姓名");
        profile.setDeptId(9L);
        when(mapper.findProfile(7L)).thenReturn(profile);
        MybatisAccountProfileQuery query = new MybatisAccountProfileQuery(mapper);
        assertSame(profile, query.findProfile(7L));
        assertNull(query.findProfile(8L));
    }

    @Test
    void invalidIdentifierDoesNotAccessPersistence() {
        AuthAccountProfileMapper mapper = mock(AuthAccountProfileMapper.class);
        MybatisAccountProfileQuery query = new MybatisAccountProfileQuery(mapper);
        assertNull(query.findProfile(null));
        assertNull(query.findProfile(0L));
        assertNull(query.findProfile(-1L));
        verifyNoInteractions(mapper);
    }

    @Test
    void profileSqlEnforcesLiveAccountWithoutDuplicateProfileDeletedColumn() throws Exception {
        Select select = AuthAccountProfileMapper.class.getMethod("findProfile", Long.class)
                .getAnnotation(Select.class);
        String sql = String.join(" ", select.value());
        assertTrue(sql.contains("JOIN SYS_ACCOUNT account ON account.ID = profile.ACCOUNT_ID"));
        assertTrue(sql.contains("COALESCE(account.DELETED, 0) = 0"));
        assertTrue(!sql.contains("profile.DELETED"));
    }
}
