package org.pkaq.sys;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.exception.BizException;
import org.pkaq.sys.user.bo.UserAoeBo;
import org.pkaq.sys.user.bo.UserQueryBo;
import org.pkaq.sys.user.convert.UserConvert;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.convert.TenantConvert;
import org.pkaq.sys.tenant.pkg.bo.TenantPackageAoeBo;
import org.pkaq.sys.tenant.pkg.convert.TenantPackageConvert;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 账号、租户及套餐的真实生成枚举转换回归。
 * @author PKAQ
 * @date 2026-10-23
 */
class AccountTenantFrozenMappingTest {
    @Test
    void mapsUserEditFrozenCodes() {
        UserAoeBo bo = new UserAoeBo();
        UserConvert convert = Mappers.getMapper(UserConvert.class);
        assertNull(convert.boToEntity((UserAoeBo) null).getFrozen());
        assertCodes(bo::setFrozen, () -> convert.boToEntity(bo).getFrozen());
    }

    @Test
    void mapsUserQueryFrozenCodes() {
        UserQueryBo bo = new UserQueryBo();
        UserConvert convert = Mappers.getMapper(UserConvert.class);
        assertNull(convert.boToEntity((UserQueryBo) null).getFrozen());
        assertCodes(bo::setFrozen, () -> convert.boToEntity(bo).getFrozen());
    }

    @Test
    void mapsTenantFrozenCodes() {
        TenantAoeBo bo = new TenantAoeBo();
        TenantConvert convert = Mappers.getMapper(TenantConvert.class);
        assertNull(convert.boToEntity(null).getFrozen());
        assertCodes(bo::setFrozen, () -> convert.boToEntity(bo).getFrozen());
    }

    @Test
    void mapsTenantPackageFrozenCodes() {
        TenantPackageAoeBo bo = new TenantPackageAoeBo();
        TenantPackageConvert convert = Mappers.getMapper(TenantPackageConvert.class);
        assertNull(convert.boToEntity(null).getFrozen());
        assertCodes(bo::setFrozen, () -> convert.boToEntity(bo).getFrozen());
    }

    private void assertCodes(Consumer<Integer> setCode, Supplier<FrozenEnumm> converted) {
        setCode.accept(null);
        assertNull(converted.get());
        for (FrozenEnumm frozen : FrozenEnumm.values()) {
            setCode.accept(frozen.getCode());
            assertEquals(frozen, converted.get());
        }
        for (int invalid : List.of(-1, 2, 3, 9998, 10000, Integer.MAX_VALUE)) {
            setCode.accept(invalid);
            assertThrows(BizException.class, converted::get);
        }
    }
}
