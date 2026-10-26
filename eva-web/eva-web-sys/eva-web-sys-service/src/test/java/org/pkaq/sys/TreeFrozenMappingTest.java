package org.pkaq.sys;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.convert.OrganizationConvert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 树模块冻结状态按业务编码而非枚举序号转换。
 *
 * @author Codex
 * @date 2026-10-18
 */
class TreeFrozenMappingTest {
    @Test
    void convertsModuleFrozenCodes() {
        ModuleConvert convert = Mappers.getMapper(ModuleConvert.class);
        ModuleAoeBo bo = new ModuleAoeBo();
        assertNull(convert.boToEntity(bo).getFrozen());
        for (FrozenEnumm frozen : FrozenEnumm.values()) {
            bo.setFrozen(frozen.getCode());
            assertEquals(frozen, convert.boToEntity(bo).getFrozen());
        }
    }

    @Test
    void convertsOrganizationFrozenCodes() {
        OrganizationConvert convert = Mappers.getMapper(OrganizationConvert.class);
        OrganizationAoeBo bo = new OrganizationAoeBo();
        assertNull(convert.aoeBoToEntity(bo).getFrozen());
        for (FrozenEnumm frozen : FrozenEnumm.values()) {
            bo.setFrozen(frozen.getCode());
            assertEquals(frozen, convert.aoeBoToEntity(bo).getFrozen());
        }
    }
}
