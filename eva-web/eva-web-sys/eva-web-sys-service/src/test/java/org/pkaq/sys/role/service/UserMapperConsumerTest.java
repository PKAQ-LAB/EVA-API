package org.pkaq.sys.role.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.mapstruct.factory.Mappers;
import org.pkaq.core.account.AccountCreationCommand;
import org.pkaq.core.account.IAccountCreation;
import org.pkaq.core.event.UserOfflineEvent;
import org.pkaq.core.mybatis.tenant.CoreSchemaExecutor;
import org.pkaq.core.mybatis.tenant.TenantSchemaResolver;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.tenant.TenantContext;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.core.upload.provider.FileProvider;
import org.pkaq.core.util.BCryptUtils;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.service.ModuleService;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.post.mapper.PostMapper;
import org.pkaq.sys.post.mapper.PostUserMapper;
import org.pkaq.sys.post.service.UserPostRefSerivce;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.user.bo.RePwdBo;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.bo.UserGrantBo;
import org.pkaq.sys.user.bo.UserPostBo;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.entity.AccountProfileEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.post.entity.PostUserEntity;
import org.pkaq.sys.user.mapper.AccountProfileMapper;
import org.pkaq.sys.user.mapper.UserMapper;
import org.pkaq.sys.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 先用C的真实自动配置映射实体，再用写入mock检查六消费者的原有控制流。
 * @author PKAQ
 * @date 2026-10-27
 */
@Isolated
class UserMapperConsumerTest {
    @AfterEach
    void cleanContext() {
        TenantContext.clear();
        assertNull(ThreadUserHelper.getCurrentUserOrNull());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"code", "frozen"})
    void editRejectsMappedProtectedAccountBeforeAnyWrite(String protection) throws Exception {
        try (var fixture = fixture()) {
            protect(fixture, protection);
            Harness harness = new Harness(UserMapperBootstrapTest.readAccount(fixture));
            UserAoeBo input = input(fixture.target, "ordinary-code");
            assertThrows(RuntimeException.class, () -> harness.service.saveUser(input));
            harness.noWrites();
            assertEquals(0, fixture.writes);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void reservedCodeCheckUsesMappedOriginalCode(boolean alreadyReserved) throws Exception {
        try (var fixture = fixture()) {
            if (alreadyReserved) {
                fixture.profileCode = "9999";
            }
            Harness harness = new Harness(UserMapperBootstrapTest.readAccount(fixture));
            assertThrows(RuntimeException.class,
                    () -> harness.service.saveUser(input(fixture.target, "9999")));
            harness.noWrites();
            // 原普通账号拒绝冒用编码；原保留账号通过编码核验后仍被编辑保护拒绝。
            if (alreadyReserved) {
                verify(harness.users).countDuplicate("synthetic-worker", "9999", fixture.target);
            } else {
                verify(harness.users, never()).countDuplicate("synthetic-worker", "9999", fixture.target);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"code", "frozen"})
    void roleAndPostGuardsRejectBeforeReadingOrChangingReferences(String protection) throws Exception {
        try (var fixture = fixture()) {
            protect(fixture, protection);
            Harness harness = new Harness(UserMapperBootstrapTest.readAccount(fixture));
            UserGrantBo grant = new UserGrantBo();
            grant.setUserId(fixture.target);
            grant.setRoleIds(List.of(12L));
            assertThrows(RuntimeException.class, () -> harness.roles.saveRoles(grant));
            UserPostBo posts = new UserPostBo();
            posts.setUserId(fixture.target);
            posts.setPostIds(List.of(31L));
            assertThrows(RuntimeException.class, () -> harness.posts.savePosts(posts));
            harness.noWrites();
            assertEquals(0, fixture.writes);
        }
    }

    @Test
    void repwdRejectsWrongOriginalPasswordWithoutWrites() throws Exception {
        try (var fixture = fixture()) {
            fixture.accountValues.put("password", BCryptUtils.hashpw("synthetic-original"));
            Harness harness = new Harness(UserMapperBootstrapTest.readAccount(fixture));
            RePwdBo input = password("synthetic-wrong");
            assertThrows(RuntimeException.class, () -> withUser(fixture.target, () -> harness.service.repwd(input)));
            harness.noWrites();
            verifyNoInteractions(harness.events);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ordinary", "code", "frozen"})
    void repwdKeepsOriginalPasswordPolicyWithoutNewReadOnlyGuard(String protection) throws Exception {
        try (var fixture = fixture()) {
            protect(fixture, protection);
            fixture.accountValues.put("password", BCryptUtils.hashpw("synthetic-original"));
            Harness harness = new Harness(UserMapperBootstrapTest.readAccount(fixture));
            doReturn(1).when(harness.users).updateById(any(UserEntity.class));
            withUser(fixture.target, () -> harness.service.repwd(password("synthetic-original")));
            ArgumentCaptor<UserEntity> updated = ArgumentCaptor.forClass(UserEntity.class);
            verify(harness.users).updateById(updated.capture());
            assertEquals(fixture.target, updated.getValue().getId());
            assertEquals(3, updated.getValue().getRevision());
            assertTrue(BCryptUtils.checkpw("synthetic-replacement", updated.getValue().getPassword()),
                    "改密仍使用原哈希流程，不输出密码或哈希");
            verify(harness.users, never()).incrementPermVer(fixture.target);
            verify(harness.events).publishEvent(any(UserOfflineEvent.class));
            verifyNoInteractions(harness.profiles, harness.roleRefs, harness.postRefs);
            assertEquals(0, fixture.writes);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void ordinaryMappedAccountCanSaveAndGrantWithOrWithoutProfile(boolean hasProfile) throws Exception {
        try (var fixture = fixture()) {
            fixture.profilePresent = hasProfile;
            UserEntity mapped = UserMapperBootstrapTest.readAccount(fixture);
            assertEquals(hasProfile ? "fixture-worker" : null, mapped.getCode());
            Harness harness = new Harness(mapped);
            doReturn(1).when(harness.users).updateById(any(UserEntity.class));
            doReturn(1).when(harness.users).incrementPermVer(fixture.target);
            if (hasProfile) {
                AccountProfileEntity existing = new AccountProfileEntity();
                existing.setAccountId(fixture.target);
                doReturn(existing).when(harness.profiles).selectById(fixture.target);
                doReturn(1).when(harness.profiles).updateById(any(AccountProfileEntity.class));
            } else {
                doReturn(1).when(harness.profiles).insert(any(AccountProfileEntity.class));
            }
            UserAoeBo input = input(fixture.target, "ordinary-code");
            input.setAvatar(mapped.getAvatar());
            harness.service.saveUser(input);
            verify(harness.users).updateById(any(UserEntity.class));
            if (hasProfile) {
                verify(harness.profiles).updateById(any(AccountProfileEntity.class));
                verify(harness.profiles, never()).insert(any(AccountProfileEntity.class));
            } else {
                verify(harness.profiles).insert(any(AccountProfileEntity.class));
            }
            UserGrantBo roles = new UserGrantBo();
            roles.setUserId(fixture.target);
            roles.setRoleIds(List.of());
            UserPostBo posts = new UserPostBo();
            posts.setUserId(fixture.target);
            posts.setPostIds(List.of());
            // 空授权集不需伪造角色/岗位可见性；已有一条关联应按原逻辑删除。
            RoleUserEntity roleRef = new RoleUserEntity();
            roleRef.setRoleId(12L);
            doReturn(List.of(roleRef)).when(harness.roleRefs).selectList(any());
            PostUserEntity postRef = new PostUserEntity();
            postRef.setPostId(31L);
            doReturn(List.of(postRef)).when(harness.postRefs).selectList(any());
            harness.roles.saveRoles(roles);
            harness.posts.savePosts(posts);
            verify(harness.roleRefs).delete(any());
            verify(harness.postRefs).delete(any());
            verify(harness.users, org.mockito.Mockito.times(3)).incrementPermVer(fixture.target);
            verify(harness.creation, never()).create(any(AccountCreationCommand.class));
            assertEquals(0, fixture.writes);
        }
    }

    private AssignedRoleDetailJdbcFixture fixture() throws Exception {
        return new AssignedRoleDetailJdbcFixture(true, false, UserMapperBootstrapFixture.Order.NORMAL);
    }

    private void protect(AssignedRoleDetailJdbcFixture fixture, String protection) {
        if ("code".equals(protection)) {
            fixture.profileCode = "9999";
        }
        if ("frozen".equals(protection)) {
            fixture.accountFrozen = 9999;
        }
    }

    private UserAoeBo input(long identifier, String code) {
        UserAoeBo input = new UserAoeBo();
        input.setId(identifier);
        input.setAccount("synthetic-worker");
        input.setCode(code);
        return input;
    }

    private RePwdBo password(String original) {
        RePwdBo input = new RePwdBo();
        input.setOriginPassword(original);
        input.setNewPassword("synthetic-replacement");
        input.setRevision(3);
        return input;
    }

    private void withUser(long identifier, Runnable operation) {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(identifier).setTenantId(0L), operation);
    }

    /**
     * 写入边界全部为mock，未知UserMapper调用直接失败，禁止掩盖保护前写入。
     * @author PKAQ
     * @date 2026-10-27
     */
    private static final class Harness {
        private final UserMapper users;
        private final RoleUserMapper roleRefs = mock(RoleUserMapper.class);
        private final PostUserMapper postRefs = mock(PostUserMapper.class);
        private final RoleMapper roleMapper = mock(RoleMapper.class);
        private final PostMapper postMapper = mock(PostMapper.class);
        private final AccountProfileMapper profiles = mock(AccountProfileMapper.class);
        private final IAccountCreation creation = mock(IAccountCreation.class);
        private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        private final UserRoleRefSerivce roles;
        private final UserPostRefSerivce posts;
        private final UserService service;

        private Harness(UserEntity mapped) {
            users = mock(UserMapper.class, invocation -> switch (invocation.getMethod().getName()) {
                case "selectById" -> {
                    assertEquals(mapped.getId(), invocation.<Object>getArgument(0));
                    yield mapped;
                }
                case "countDuplicate" -> 0L;
                default -> throw new AssertionError("写保护前发生未许可Mapper调用: "
                        + invocation.getMethod().getName());
            });
            EvaConfig config = new EvaConfig();
            JdbcTemplate jdbc = mock(JdbcTemplate.class);
            CoreSchemaExecutor core = mock(CoreSchemaExecutor.class);
            roles = new UserRoleRefSerivce(roleRefs, roleMapper, users, jdbc, config,
                    mock(TenantSchemaResolver.class), core);
            posts = new UserPostRefSerivce(postRefs, postMapper, users);
            service = new UserService(roles, posts, mock(FileProvider.class), roleRefs, postRefs,
                    mock(OrganizationMapper.class), mock(ModuleService.class), mock(ModuleConvert.class),
                    Mappers.getMapper(UserConvert.class), config, events, core,
                    mock(RoleResourceMapper.class), creation, profiles);
            ReflectionTestUtils.setField(service, "mapper", users);
        }

        private void noWrites() {
            verify(users, never()).updateById(any(UserEntity.class));
            verify(users, never()).insert(any(UserEntity.class));
            verify(users, never()).incrementPermVer(any());
            verify(creation, never()).create(any(AccountCreationCommand.class));
            verifyNoInteractions(profiles, roleRefs, postRefs, roleMapper, postMapper, events);
        }
    }
}
