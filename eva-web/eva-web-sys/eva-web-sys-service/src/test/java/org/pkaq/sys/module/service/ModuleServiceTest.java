package org.pkaq.sys.module.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.entity.ModuleResources;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.mapper.ModuleResourceMapper;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.module.vo.ModuleResourcesVo;
import org.pkaq.sys.role.convert.RoleConvert;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 模块详情查询的转换链与错误语义回归测试。
 *
 * @author Codex
 * @date 2026-10-13
 */
@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {
    @Mock
    private ModuleMapper moduleMapper;
    @Mock
    private ModuleResourceMapper resourceMapper;
    @Mock
    private RoleResourceMapper roleResourceMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ModuleService service;

    /**
     * 使用真实生成转换器，复现父类注入角色转换器的运行时环境。
     */
    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                ModuleResources.class);
        ModuleConvert moduleConvert = Mappers.getMapper(ModuleConvert.class);
        RoleConvert genericConvert = Mappers.getMapper(RoleConvert.class);
        ReflectionTestUtils.invokeMethod(genericConvert, "init");
        service = new ModuleService(resourceMapper, roleResourceMapper, moduleConvert, eventPublisher);
        ReflectionTestUtils.setField(service, "mapper", moduleMapper);
        ReflectionTestUtils.setField(service, StdService.class, "convert", genericConvert, null);
    }

    /**
     * 详情必须保留树路径、路由、状态和资源，并仅查询当前模块资源。
     */
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldReturnDetailWithResources() {
        ModuleEntity entity = new ModuleEntity();
        entity.setId(7L);
        entity.setPid(2L);
        entity.setPath("/2/7");
        entity.setCode("module");
        entity.setName("模块管理");
        entity.setIcon("SettingOutlined");
        entity.setRouteUrl("/sys/module");
        entity.setComponentUrl("./sys/module");
        entity.setIsleaf(true);
        entity.setFrozen(FrozenEnumm.READ_ONLY);
        entity.setTenantId(101L);
        entity.setRevision(3);
        entity.setSort(4D);
        ModuleResources resource = new ModuleResources();
        resource.setId(11L);
        resource.setMainId(7L);
        resource.setCode("edit");
        resource.setResourceDesc("编辑模块");
        resource.setResourceUrl("/sys/module/edit");
        resource.setResourceType("operation");
        resource.setSort(2D);
        when(moduleMapper.selectById(7L)).thenReturn(entity);
        when(resourceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(resource));

        ModuleDetailVo detail = service.getModule(7L);

        assertEquals(entity.getId(), detail.getId());
        assertEquals(entity.getPid(), detail.getPid());
        assertEquals(entity.getPath(), detail.getPath());
        assertEquals(entity.getCode(), detail.getCode());
        assertEquals(entity.getName(), detail.getName());
        assertEquals(entity.getIcon(), detail.getIcon());
        assertEquals(entity.getRouteUrl(), detail.getRouteUrl());
        assertEquals(entity.getComponentUrl(), detail.getComponentUrl());
        assertEquals(entity.getIsleaf(), detail.getIsleaf());
        assertEquals(entity.getFrozen(), detail.getFrozen());
        assertEquals(entity.getTenantId(), detail.getTenantId());
        assertEquals(3, detail.getRevision());
        assertEquals(4D, detail.getSort());
        assertEquals(1, detail.getResources().size());
        ModuleResourcesVo resourceVo = detail.getResources().getFirst();
        assertEquals(resource.getId(), resourceVo.getId());
        assertEquals(resource.getMainId(), resourceVo.getMainId());
        assertEquals(resource.getCode(), resourceVo.getCode());
        assertEquals(resource.getResourceDesc(), resourceVo.getResourceDesc());
        assertEquals(resource.getResourceUrl(), resourceVo.getResourceUrl());
        assertEquals(resource.getResourceType(), resourceVo.getResourceType());
        assertEquals(resource.getSort(), resourceVo.getSort());
        ArgumentCaptor<LambdaQueryWrapper<ModuleResources>> query =
                ArgumentCaptor.forClass((Class) LambdaQueryWrapper.class);
        verify(resourceMapper).selectList(query.capture());
        assertTrue(query.getValue().getSqlSegment().contains("main_id ="));
        assertEquals(List.of(7L), List.copyOf(query.getValue().getParamNameValuePairs().values()));
        verifyNoInteractions(roleResourceMapper, eventPublisher);
    }

    /**
     * 没有资源的模块详情必须保留空资源列表。
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyResources() {
        ModuleEntity entity = new ModuleEntity();
        entity.setId(7L);
        when(moduleMapper.selectById(7L)).thenReturn(entity);
        when(resourceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        assertEquals(List.of(), service.getModule(7L).getResources());
    }

    /**
     * 零编号必须保留参数错误，且不得访问数据库。
     */
    @Test
    void shouldRejectZeroId() {
        BizException error = assertThrows(BizException.class, () -> service.getModule(0L));

        assertEquals(CommonCodes.PARAM_ERROR, error.getBizCode());
        verifyNoInteractions(moduleMapper, resourceMapper, roleResourceMapper, eventPublisher);
    }

    /**
     * 记录不存在时必须保留原错误码，且不得继续查询资源。
     */
    @Test
    void shouldRejectMissingModule() {
        when(moduleMapper.selectById(7L)).thenReturn(null);

        BizException error = assertThrows(BizException.class, () -> service.getModule(7L));

        assertEquals(CommonCodes.CAN_NOT_FIND_RECORD, error.getBizCode());
        verifyNoInteractions(resourceMapper, roleResourceMapper, eventPublisher);
    }
}
