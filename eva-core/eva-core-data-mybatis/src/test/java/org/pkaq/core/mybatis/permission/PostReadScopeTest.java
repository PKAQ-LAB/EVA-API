package org.pkaq.core.mybatis.permission;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;
import org.pkaq.config.MybatisPlusDataPermissionHandler;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 真实AOP与MP解析验证纯读用途、主体绑定和多角色并集。
 * @author PKAQ
 * @date 2026-11-01
 */
public class PostReadScopeTest {
    @RestController
    public static class Reader {
        @PostScopeRead
        public void read(Runnable action) {
            action.run();
        }
    }

    @Test
    void requiresControllerWindowReadonlyTransactionAndActualSelect() {
        EvaConfig config = new EvaConfig();
        config.getDataPermission().setEnable(true);
        ThreadUser user = new ThreadUser().setUserId(7L).setTenantId(0L)
                .setDataScopes(List.of(new ThreadUser.DataScope("0006", List.of(), List.of(11L), true)));
        AspectJProxyFactory proxy = new AspectJProxyFactory(new Reader());
        proxy.addAspect(new PostScopeReadAspect());
        Reader reader = proxy.getProxy();
        ThreadUserHelper.runWithUser(user, () -> {
            try {
                assertTrue(sql(config).contains("1 = 0"));
                TransactionSynchronizationManager.setActualTransactionActive(true);
                TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
                assertTrue(sql(config).contains("1 = 0"));
                reader.read(() -> assertTrue(sql(config).contains("1 = 1")));
                assertFalse(PostReadScope.allows("query"));
                TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
                reader.read(() -> assertTrue(sql(config).contains("1 = 0")));
                user.getDataScopes().getFirst().setPostEligible(false);
                TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
                reader.read(() -> assertTrue(sql(config).contains("1 = 0")));
                user.setDataScopes(List.of(new ThreadUser.DataScope("0006", List.of()),
                        new ThreadUser.DataScope("0007", List.of())));
                reader.read(() -> assertTrue(sql(config).contains("CREATE_ID = 7")));
                user.setDataScopes(List.of(new ThreadUser.DataScope("0006", List.of()),
                        new ThreadUser.DataScope("0000", List.of())));
                assertTrue(sql(config).contains("1 = 1"));
            } finally {
                TransactionSynchronizationManager.clear();
            }
        });
    }

    @Test
    void bindsSubjectAndRestoresWindowOnFailureWithoutAsyncInheritance() {
        ThreadUser user = new ThreadUser().setUserId(7L).setTenantId(1L);
        ThreadUserHelper.runWithUser(user, () -> {
            TransactionSynchronizationManager.setActualTransactionActive(true);
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
            try (PostReadScope.Window ignored = PostReadScope.select("query")) {
                assertThrows(IllegalStateException.class, () -> {
                    try (PostReadScope.Window read = PostReadScope.read()) {
                        assertTrue(PostReadScope.allows("query"));
                        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setTenantId(1L),
                                () -> assertFalse(PostReadScope.allows("query")));
                        Thread thread = new Thread(() -> assertFalse(PostReadScope.allows("query")));
                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException exception) {
                            throw new IllegalStateException(exception);
                        }
                        throw new IllegalStateException("合成异常");
                    }
                });
                assertFalse(PostReadScope.allows("query"));
            } finally {
                TransactionSynchronizationManager.clear();
            }
        });
    }

    private String sql(EvaConfig config) {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MappedStatement statement = new MappedStatement.Builder(configuration, "query",
                new StaticSqlSource(configuration, "SELECT id FROM scoped_record"), SqlCommandType.SELECT).build();
        BoundSql sql = statement.getBoundSql(null);
        try {
            new PostScopeDataPermissionInterceptor(new MybatisPlusDataPermissionHandler(config))
                    .beforeQuery(null, statement, null, RowBounds.DEFAULT, null, sql);
        } catch (java.sql.SQLException exception) {
            throw new IllegalStateException(exception);
        }
        return sql.getSql();
    }
}
