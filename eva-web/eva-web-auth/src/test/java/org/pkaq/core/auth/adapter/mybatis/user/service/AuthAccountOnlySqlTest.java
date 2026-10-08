package org.pkaq.core.auth.adapter.mybatis.user.service;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.pkaq.core.auth.adapter.mybatis.user.entity.AuthUserEntity;
import org.pkaq.core.auth.adapter.mybatis.user.mapper.AuthUserMapper;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 纯账号查询不得依赖租户、角色或可选管理资料表。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
class AuthAccountOnlySqlTest {
    @Test
    void loginSqlHasOnlyAccountPersistenceDependency() throws Exception {
        Configuration configuration = new Configuration();
        configuration.getTypeAliasRegistry().registerAlias("authUser", AuthUserEntity.class);
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("mapper/AuthUser.xml")) {
            new XMLMapperBuilder(input, configuration, "mapper/AuthUser.xml", configuration.getSqlFragments()).parse();
        }
        AuthUserEntity condition = new AuthUserEntity();
        condition.setAccount("demo");
        String namespace = AuthUserMapper.class.getName() + ".";
        assertOnlyAccountTable(configuration.getMappedStatement(namespace + "getUserAccount")
                .getBoundSql(condition).getSql());
        assertOnlyAccountTable(configuration.getMappedStatement(namespace + "getTenantUserAccount")
                .getBoundSql(condition).getSql());
    }

    @Test
    void accountStateSqlHasOnlyAccountPersistenceDependency() throws Exception {
        for (String method : new String[]{"getAuthState", "getTenantAuthState"}) {
            Select annotation = AuthUserMapper.class.getMethod(method, Long.class).getAnnotation(Select.class);
            assertOnlyAccountTable(String.join(" ", annotation.value()));
        }
    }

    private void assertOnlyAccountTable(String sql) {
        assertTrue(sql.contains("SYS_ACCOUNT"));
        assertFalse(sql.contains("SYS_TENANT"));
        assertFalse(sql.contains("SYS_ROLE"));
        assertFalse(sql.contains("SYS_ACCOUNT_PROFILE"));
        assertFalse(sql.contains("JOIN"));
    }
}
