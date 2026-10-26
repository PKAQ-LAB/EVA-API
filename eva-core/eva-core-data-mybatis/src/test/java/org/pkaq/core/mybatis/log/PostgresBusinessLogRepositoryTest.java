package org.pkaq.core.mybatis.log;

import org.junit.jupiter.api.Test;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.log.base.BizLogEntity;
import org.pkaq.core.log.bo.LogQueryBo;
import org.pkaq.core.mybatis.log.mapper.BusinessLogMapper;
import org.pkaq.core.mybatis.log.entity.MybatisBizLogEntity;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.reset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

/**
 * 业务日志读取边界回归。
 * @author PKAQ
 * @date 2026-10-23
 */
class PostgresBusinessLogRepositoryTest {
    private final BusinessLogMapper mapper = mock(BusinessLogMapper.class);
    private final EvaConfig config = new EvaConfig();
    private final PostgresBusinessLogRepository repository = new PostgresBusinessLogRepository(mapper, config);

    @Test
    void deniesMissingAndRestrictedIdentityBeforeListCountDetailOrArchiveQuery() {
        assertThrows(RuntimeException.class, () -> repository.get("invalid"));
        assertThrows(RuntimeException.class, () -> repository.list(null));
        for (String code : Arrays.asList(null, "", "unknown", "0001", "0002", "0003", "0004", "0005", "0006", "0007")) {
            ThreadUser user = user().setDataScopes(List.of(new ThreadUser.DataScope(code, List.of())));
            ThreadUserHelper.runWithUser(user, () -> {
                assertThrows(RuntimeException.class, () -> repository.get("1"));
                assertThrows(RuntimeException.class, () -> repository.list(new LogQueryBo()));
            });
        }
        for (ThreadUser user : List.of(user().setUserId(0L), user().setTenantId(-1L),
                user().setDataScopes(null), user().setDataScopes(List.of()))) {
            ThreadUserHelper.runWithUser(user, () -> assertThrows(RuntimeException.class, () -> repository.list(null)));
        }
        verifyNoInteractions(mapper);
    }

    @Test
    void keepsTrustedTenantForDetailCountAndBothArchiveModes() {
        config.setMode(CommonConstant.MODE_SAAS);
        for (boolean archived : List.of(true, false)) {
            reset(mapper);
            LogQueryBo query = new LogQueryBo();
            query.setTargetTenantId(99L);
            query.setIncludeArchived(archived);
            ThreadUserHelper.runWithUser(user(), () -> {
                repository.list(query);
                repository.get("1");
            });
            verify(mapper).selectLogById(1L, 12L);
            verify(mapper).countLogs(eq(12L), anyString(), anyString(), eq(false),
                    isNull(), isNull(), isNull(), isNull(), eq(archived));
            verify(mapper).selectLogPage(eq(12L), anyString(), anyString(), eq(false),
                    isNull(), isNull(), isNull(), isNull(),
                    eq(archived), eq(0L), eq((long) query.getPageSize()));
        }
    }

    @Test
    void keepsStandaloneAndExistingPlatformTargetRulesWithMixedAllScope() {
        config.setMode(CommonConstant.MODE_STANDALONE);
        ThreadUserHelper.runWithUser(user(), () -> repository.get("1"));
        verify(mapper).selectLogById(1L, 0L);
        reset(mapper);
        config.setMode(CommonConstant.MODE_PLATFORM);
        ThreadUser admin = user().setRolesMap(java.util.Map.of(1L,
                        new ThreadUser.GrantedRoles("平台管理员", CommonConstant.ADMIN_ROLE_NAME)))
                .setDataScopes(Arrays.asList(null, new ThreadUser.DataScope("0007", List.of()),
                        new ThreadUser.DataScope("0000", List.of())));
        for (Long target : Arrays.asList(null, 23L)) {
            reset(mapper);
            LogQueryBo query = new LogQueryBo();
            query.setTargetTenantId(target);
            ThreadUserHelper.runWithUser(admin, () -> repository.list(query));
            verify(mapper).countLogs(eq(target), anyString(), anyString(), eq(false),
                    isNull(), isNull(), isNull(), isNull(), eq(true));
        }
    }

    @Test
    void keepsAnonymousBusinessLogWrites() {
        repository.save(new BizLogEntity());
        verify(mapper).insert(any(MybatisBizLogEntity.class));
    }

    private ThreadUser user() {
        return new ThreadUser().setUserId(11L).setTenantId(12L)
                .setDataScopes(List.of(new ThreadUser.DataScope("0000", List.of())));
    }
}
