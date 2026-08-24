package org.pkaq.sys.dict.convert;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictLineBo;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.vo.DictLineVo;
import org.pkaq.sys.dict.vo.DictViewVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 字典对象转换器测试。
 *
 * @author PKAQ
 */
class DictConvertTest {
    private final DictConvert convert = Mappers.getMapper(DictConvert.class);

    /**
     * 验证字典编辑参数能够完整转换为实体。
     */
    @Test
    void shouldConvertBoToEntity() {
        DictAoeBo bo = new DictAoeBo();
        bo.setId(1L);
        bo.setRevision(2);
        bo.setFrozen(FrozenEnumm.READ_ONLY.getCode());
        bo.setSort(3D);
        bo.setRemark("只读字典");
        bo.setType("system");
        bo.setCode("status");
        bo.setName("状态");

        DictEntity entity = this.convert.boToEntity(bo);

        assertEquals(bo.getId(), entity.getId());
        assertEquals(bo.getRevision(), entity.getRevision());
        assertEquals(FrozenEnumm.READ_ONLY, entity.getFrozen());
        assertEquals(bo.getSort(), entity.getSort());
        assertEquals(bo.getRemark(), entity.getRemark());
        assertEquals(bo.getType(), entity.getType());
        assertEquals(bo.getCode(), entity.getCode());
        assertEquals(bo.getName(), entity.getName());
    }

    /**
     * 验证字典明细字段和锁定状态能够双向转换。
     */
    @Test
    void shouldConvertLineInBothDirections() {
        DictLineBo bo = new DictLineBo();
        bo.setId(11L);
        bo.setKeyName("enabled");
        bo.setKeyValue("启用");
        bo.setOrders(5D);
        bo.setFrozen(FrozenEnumm.FROZEN.getCode());
        bo.setRemark("不可选择");

        DictItemEntity entity = this.convert.lineBoToEntity(bo);
        DictLineVo vo = this.convert.lineEntityToVo(entity);

        assertEquals(bo.getId(), entity.getId());
        assertEquals(bo.getKeyName(), entity.getDCode());
        assertEquals(bo.getKeyValue(), entity.getDValue());
        assertEquals(bo.getOrders(), entity.getSort());
        assertEquals(FrozenEnumm.FROZEN, entity.getFrozen());
        assertEquals(bo.getRemark(), entity.getRemark());
        assertEquals(bo.getId(), vo.getId());
        assertEquals(bo.getKeyName(), vo.getKeyName());
        assertEquals(bo.getKeyValue(), vo.getKeyValue());
        assertEquals(bo.getOrders(), vo.getOrders());
        assertEquals(bo.getFrozen(), vo.getFrozen());
        assertEquals(bo.getRemark(), vo.getRemark());
    }

    /**
     * 验证视图转换不会由 MapStruct 自动填充明细列表。
     */
    @Test
    void shouldLeaveViewLinesEmptyForServiceAssembly() {
        DictEntity entity = new DictEntity();
        entity.setId(1L);
        entity.setCode("status");
        entity.setName("状态");
        entity.setFrozen(FrozenEnumm.UN_FROZEN);

        DictViewVo vo = this.convert.entityToVo(entity);

        assertEquals(entity.getId(), vo.getId());
        assertEquals(entity.getCode(), vo.getCode());
        assertEquals(entity.getName(), vo.getName());
        assertEquals(FrozenEnumm.UN_FROZEN.getCode(), vo.getFrozen());
        assertNotNull(vo.getLines());
        assertTrue(vo.getLines().isEmpty());
    }

    /**
     * 验证批量转换保持明细顺序。
     */
    @Test
    void shouldKeepLineOrderWhenConvertingList() {
        DictItemEntity first = new DictItemEntity();
        first.setDCode("first");
        DictItemEntity second = new DictItemEntity();
        second.setDCode("second");

        List<DictLineVo> result = this.convert.lineEntityToVo(List.of(first, second));

        assertEquals(2, result.size());
        assertEquals("first", result.get(0).getKeyName());
        assertEquals("second", result.get(1).getKeyName());
    }

    /**
     * 验证集合转换遇到空输入时统一返回空集合。
     */
    @Test
    void shouldReturnEmptyListWhenSourceListIsNull() {
        List<DictLineVo> result = this.convert.lineEntityToVo((List<DictItemEntity>) null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
