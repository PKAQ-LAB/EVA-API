package org.pkaq.sys;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.sys.organization.convert.OrganizationConvert;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.organization.service.OrganizationService;
import org.pkaq.sys.post.convert.PostConvert;
import org.pkaq.sys.post.entity.PostEntity;
import org.pkaq.sys.post.mapper.PostMapper;
import org.pkaq.sys.post.mapper.PostUserMapper;
import org.pkaq.sys.post.service.PostService;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.tenant.mapper.TenantAuthorizationMapper;
import org.pkaq.sys.tenant.pkg.convert.TenantPackageConvert;
import org.pkaq.sys.tenant.pkg.entity.TenantPackageEntity;
import org.pkaq.sys.tenant.pkg.mapper.TenantPackageMapper;
import org.pkaq.sys.tenant.pkg.mapper.TenantPackageResourceMapper;
import org.pkaq.sys.tenant.pkg.service.TenantPackageService;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 系统管理写入口保护测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class WriteProtectionServiceTest {
    @Mock
    private OrganizationMapper organizationMapper;
    @Mock
    private OrganizationConvert organizationConvert;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PostMapper postMapper;
    @Mock
    private PostConvert postConvert;
    @Mock
    private PostUserMapper postUserMapper;
    @Mock
    private TenantPackageMapper tenantPackageMapper;
    @Mock
    private TenantPackageResourceMapper tenantPackageResourceMapper;
    @Mock
    private RoleResourceMapper roleResourceMapper;
    @Mock
    private TenantAuthorizationMapper tenantAuthorizationMapper;
    @Mock
    private TenantPackageConvert tenantPackageConvert;

    private OrganizationService organizationService;
    private PostService postService;
    private TenantPackageService tenantPackageService;

    @BeforeEach
    void setUp() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), OrganizationEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), PostEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), TenantPackageEntity.class);
        this.organizationService = new OrganizationService(this.organizationConvert, this.userMapper);
        ReflectionTestUtils.setField(this.organizationService, "mapper", this.organizationMapper);
        this.postService = new PostService(this.postConvert, this.postUserMapper);
        ReflectionTestUtils.setField(this.postService, "mapper", this.postMapper);
        this.tenantPackageService = new TenantPackageService(this.tenantPackageMapper,
                this.tenantPackageResourceMapper, this.roleResourceMapper,
                this.tenantAuthorizationMapper, this.tenantPackageConvert);
    }

    @Test
    void rejectsDeletingReadOnlyOrganization() {
        when(this.organizationMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> this.organizationService.delete(Set.of(1L)));

        verify(this.organizationMapper, never()).delete(any(Wrapper.class));
    }

    @Test
    void rejectsDeletingReadOnlyPost() {
        when(this.postMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> this.postService.del(Set.of(1L)));

        verify(this.postMapper, never()).delete(any(Wrapper.class));
    }

    @Test
    void rejectsDeletingReadOnlyTenantPackage() {
        when(this.tenantPackageMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> this.tenantPackageService.delete(Set.of(1L)));

        verify(this.tenantPackageMapper, never()).deleteByIds(any());
    }

    @Test
    void rejectsDeletingTenantPackageWithActiveGrant() {
        when(this.tenantPackageMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(this.tenantAuthorizationMapper.countActivePackageGrants(Set.of(1L))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> this.tenantPackageService.delete(Set.of(1L)));

        verify(this.tenantPackageMapper, never()).deleteByIds(any());
        verify(this.tenantPackageResourceMapper, never()).delete(any(Wrapper.class));
    }
}
