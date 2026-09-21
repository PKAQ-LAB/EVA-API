package org.pkaq.sys.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.role.bo.RoleUserRefBo;
import org.pkaq.sys.role.convert.RoleConvert;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.role.entity.RoleUserEntity;
import org.pkaq.sys.role.mapper.RoleMapper;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.role.mapper.RoleUserMapper;
import org.pkaq.sys.role.vo.RoleGrantedUserVo;
import org.pkaq.sys.tenant.mapper.TenantResourceMapper;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色用户授权的数据权限测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private RoleResourceMapper roleResourceMapper;
    @Mock
    private RoleUserMapper roleUserMapper;
    @Mock
    private ModuleMapper moduleMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TenantResourceMapper tenantResourceMapper;
    @Mock
    private UserConvert userConvert;
    @Mock
    private RoleConvert roleConvert;
    @Mock
    private ModuleConvert moduleConvert;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private RoleService service;

    /**
     * 初始化被测服务。
     */
    @BeforeEach
    void setUp() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), RoleUserEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), UserEntity.class);
        service = new RoleService(roleResourceMapper, roleUserMapper, moduleMapper, userMapper,
                tenantResourceMapper, userConvert, roleConvert, moduleConvert, eventPublisher);
        ReflectionTestUtils.setField(service, "mapper", roleMapper);
    }

    /**
     * 角色详情只返回当前数据权限范围内的已选用户。
     */
    @Test
    void shouldHideCheckedUsersOutsideDataScope() {
        when(roleMapper.selectById(5L)).thenReturn(new RoleEntity());
        UserEntity visibleUser = new UserEntity();
        visibleUser.setId(10L);
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(visibleUser));
        when(roleUserMapper.selectObjs(any(LambdaQueryWrapper.class))).thenReturn(List.of(10L, 20L));
        when(userConvert.entityToSimpleVo(List.of(visibleUser))).thenReturn(List.of());
        RoleGrantedUserVo expected = new RoleGrantedUserVo();
        when(roleConvert.toGrantedUserVo(List.of(), Set.of(10L))).thenReturn(expected);

        RoleGrantedUserVo result = service.listUser(5L, null);

        assertEquals(expected, result);
        verify(roleConvert).toGrantedUserVo(List.of(), Set.of(10L));
    }

    /**
     * 保存授权时不得撤销当前数据权限范围外的既有用户。
     */
    @Test
    void shouldPreserveExistingUsersOutsideDataScope() {
        when(roleMapper.selectById(5L)).thenReturn(new RoleEntity());
        when(roleUserMapper.selectObjs(any(LambdaQueryWrapper.class))).thenReturn(List.of(10L, 20L));
        UserEntity visibleUser = new UserEntity();
        visibleUser.setId(10L);
        when(userMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(visibleUser), List.of(visibleUser));
        RoleUserRefBo input = new RoleUserRefBo();
        input.setRoleId(5L);
        input.setUserId(List.of(10L));

        service.grantUser(input);

        verify(roleUserMapper, never()).delete(any(LambdaQueryWrapper.class));
        verify(roleUserMapper, never()).insert(any(RoleUserEntity.class));
        verify(userMapper, never()).incrementPermVer(any());
    }
}
