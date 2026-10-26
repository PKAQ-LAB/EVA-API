package org.pkaq.sys;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.exception.BizException;
import org.pkaq.sys.role.bo.RoleAoeBo;
import org.pkaq.sys.role.convert.RoleConvert;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 角色冻结状态的真实MapStruct生成转换回归。
 * @author PKAQ
 * @date 2026-10-23
 */
class RoleFrozenMappingTest {
    private final RoleConvert convert = Mappers.getMapper(RoleConvert.class);

    @Test
    void mapsBusinessCodesInsteadOfOrdinalsAndKeepsNull() {
        assertNull(convert.fromBo((RoleAoeBo) null).getFrozen());
        RoleAoeBo bo = new RoleAoeBo();
        assertNull(convert.fromBo(bo).getFrozen());
        for (FrozenEnumm frozen : FrozenEnumm.values()) {
            bo.setFrozen(frozen.getCode());
            assertEquals(frozen, convert.fromBo(bo).getFrozen());
        }
    }

    @Test
    void rejectsUnknownBusinessCodesIncludingOrdinalTwo() {
        for (int code : List.of(-1, 2, 3, 9998, 10000, Integer.MAX_VALUE)) {
            RoleAoeBo bo = new RoleAoeBo();
            bo.setFrozen(code);
            assertThrows(BizException.class, () -> convert.fromBo(bo));
        }
    }
}
