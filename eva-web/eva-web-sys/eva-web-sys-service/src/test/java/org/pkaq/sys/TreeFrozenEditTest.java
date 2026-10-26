package org.pkaq.sys;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.bo.ModuleFrozenBo;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.mapper.ModuleResourceMapper;
import org.pkaq.sys.module.service.ModuleService;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.convert.OrganizationConvert;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.organization.service.OrganizationService;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 普通编辑保持原冻结状态，显式冻结接口仍按目标编码执行。
 *
 * @author Codex
 * @date 2026-10-18
 */
class TreeFrozenEditTest {
    @Test
    void moduleEditPreservesFrozenStateAndSwitchRemainsExplicit() {
        ModuleMapper mapper = mock(ModuleMapper.class);
        ModuleService service = new ModuleService(mock(ModuleResourceMapper.class), mock(RoleResourceMapper.class),
                Mappers.getMapper(ModuleConvert.class), mock(ApplicationEventPublisher.class));
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ModuleEntity original = new ModuleEntity();
        original.setId(7L);
        original.setPid(0L);
        original.setPath("/7");
        original.setSort(1D);
        original.setFrozen(FrozenEnumm.UN_FROZEN);
        when(mapper.selectById(7L)).thenReturn(original);
        ModuleAoeBo bo = new ModuleAoeBo();
        bo.setId(7L);
        bo.setPid(0L);
        bo.setFrozen(1);

        service.editModule(bo);

        ArgumentCaptor<ModuleEntity> saved = ArgumentCaptor.forClass(ModuleEntity.class);
        verify(mapper).updateById(saved.capture());
        assertEquals(FrozenEnumm.UN_FROZEN, saved.getValue().getFrozen());
        ModuleFrozenBo target = new ModuleFrozenBo();
        target.setParam(Set.of(7L));
        target.setFrozen(1);
        service.switchFrozen(target);
        verify(mapper).cascadeFrozen(7L, "/7", 1);
    }

    @Test
    void organizationEditPreservesFrozenState() {
        OrganizationMapper mapper = mock(OrganizationMapper.class);
        OrganizationService service = new OrganizationService(Mappers.getMapper(OrganizationConvert.class),
                mock(UserMapper.class));
        ReflectionTestUtils.setField(service, "mapper", mapper);
        OrganizationEntity original = new OrganizationEntity();
        original.setId(7L);
        original.setPid(0L);
        original.setPath("/7");
        original.setSort(1D);
        original.setFrozen(FrozenEnumm.UN_FROZEN);
        when(mapper.selectById(7L)).thenReturn(original);
        OrganizationAoeBo bo = new OrganizationAoeBo();
        bo.setId(7L);
        bo.setPid(0L);
        bo.setFrozen(1);

        service.edit(bo);

        ArgumentCaptor<OrganizationEntity> saved = ArgumentCaptor.forClass(OrganizationEntity.class);
        verify(mapper).updateById(saved.capture());
        assertEquals(FrozenEnumm.UN_FROZEN, saved.getValue().getFrozen());
    }
}
