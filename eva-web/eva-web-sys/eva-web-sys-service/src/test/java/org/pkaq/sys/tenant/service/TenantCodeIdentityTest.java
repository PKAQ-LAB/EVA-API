package org.pkaq.sys.tenant.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.TenantProperties;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.bo.TenantCheckBo;
import org.pkaq.sys.tenant.convert.TenantConvert;
import org.pkaq.sys.tenant.entity.TenantEntity;
import org.pkaq.sys.tenant.mapper.TenantMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 租户编码不可变和历史占用校验，不连接数据库。
 *
 * @author PKAQ
 * @date 2026-10-09
 */
class TenantCodeIdentityTest {
    private final TenantMapper mapper = mock(TenantMapper.class);
    private final TenantConvert convert = mock(TenantConvert.class);
    private TenantService service;

    /** 仅装配当前编码路径所需依赖。 */
    @BeforeEach
    void setUp() {
        // 隔离 Mockito 不启动 Mapper 扫描，需显式建立 Lambda 字段元数据。
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "tenant-test"),
                TenantEntity.class);
        EvaConfig config = new EvaConfig();
        config.setMode("platform");
        config.getTenant().setEnable(true);
        config.getTenant().setMode(TenantProperties.MODE_SCHEMA);
        service = new TenantService(null, null, null, convert, null, null, null, null, config);
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    /** 编码必须原样合法，禁止静默转小写、裁剪空白或接受非 ASCII。 */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"A123", " t1", "t1 ", "tenant7", "租户", "a-b", "a.b", "a/b", "1abc", "_abc"})
    void rejectsInvalidRawCodeBeforeStorage(String code) {
        TenantAoeBo bo = input();
        bo.setCode(code);

        BizException failure = assertThrows(BizException.class, () -> service.edit(bo));

        assertEquals(SysCodes.TENANT_CODE_INVALID, failure.getBizCode());
        verify(mapper, never()).insert(any(TenantEntity.class));
    }

    /** 历史软删除租户仍占用 code，新建不能复用该数据身份。 */
    @Test
    void historicalCodeBlocksNewTenant() {
        when(mapper.countCodeAll("t1", null)).thenReturn(1L);

        BizException failure = assertThrows(BizException.class, () -> service.edit(input()));

        assertEquals(SysCodes.TENANT_CODE_ALREADY_EXIST, failure.getBizCode());
        verify(mapper, never()).insert(any(TenantEntity.class));
    }

    /** 修改 code 明确报错，而不是静默忽略前端提交。 */
    @Test
    void changedCodeIsRejected() {
        TenantAoeBo bo = input();
        bo.setId(101L);
        TenantEntity existing = existing();
        existing.setCode("old");
        when(mapper.selectById(101L)).thenReturn(existing);

        BizException failure = assertThrows(BizException.class, () -> service.edit(bo));

        assertEquals(SysCodes.TENANT_CODE_IMMUTABLE, failure.getBizCode());
        verify(mapper, never()).updateById(any(TenantEntity.class));
    }

    /** 相同 code 可编辑普通资料，写库时仍不修改编码。 */
    @Test
    void unchangedCodeAllowsOrdinaryEdit() {
        TenantAoeBo bo = input();
        bo.setId(101L);
        TenantEntity existing = existing();
        when(mapper.selectById(101L)).thenReturn(existing);
        when(mapper.selectOne(any())).thenReturn(existing);
        TenantEntity update = new TenantEntity();
        update.setId(101L);
        when(convert.boToEntity(bo)).thenReturn(update);

        service.edit(bo);

        verify(mapper).countCodeAll("t1", 101L);
        verify(mapper).updateById(update);
        assertEquals(null, update.getCode());
        assertEquals(101L, bo.getId());
    }

    /** 前端查重也覆盖软删除记录，编辑仅排除自身 ID。 */
    @Test
    void uniquenessIncludesHistoricalCode() {
        TenantCheckBo check = new TenantCheckBo();
        check.setCode("t1");
        when(mapper.countCodeAll("t1", null)).thenReturn(1L);

        assertTrue(service.checkUnique(check));
    }

    private TenantAoeBo input() {
        TenantAoeBo bo = new TenantAoeBo();
        bo.setCode("t1");
        bo.setAdminPass("test-password");
        bo.setAuthUserCount(2);
        bo.setExpirationDate(new Date(System.currentTimeMillis() + 86_400_000L));
        return bo;
    }

    private TenantEntity existing() {
        TenantEntity entity = new TenantEntity();
        entity.setId(101L);
        entity.setCode("t1");
        entity.setFrozen(FrozenEnumm.UN_FROZEN);
        entity.setAuthUserCount(2);
        return entity;
    }
}
