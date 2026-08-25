package org.pkaq.sys.dict.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictLineBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.convert.DictConvert;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.entity.DictItemEntity;
import org.pkaq.sys.dict.mapper.DictItemMapper;
import org.pkaq.sys.dict.mapper.DictMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 字典服务测试。
 *
 * @author PKAQ
 */
@ExtendWith(MockitoExtension.class)
class DictServiceTest {
    @Mock
    private DictMapper dictMapper;

    @Mock
    private DictItemMapper dictItemMapper;

    @Mock
    private DictCacheHelper dictCacheHelper;

    private DictService service;

    /**
     * 初始化待测服务和事务同步上下文。
     */
    @BeforeEach
    void setUp() {
        DictConvert convert = Mappers.getMapper(DictConvert.class);
        this.service = new DictService(this.dictCacheHelper, this.dictItemMapper, convert);
        ReflectionTestUtils.setField(this.service, "mapper", this.dictMapper);
        TransactionSynchronizationManager.initSynchronization();
    }

    /**
     * 清理事务同步上下文。
     */
    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    /**
     * 验证编辑字典时按差异更新明细，并延迟刷新缓存。
     */
    @Test
    void shouldDiffSaveItemsWhenEditingDict() {
        DictEntity oldDict = new DictEntity();
        oldDict.setId(1L);
        oldDict.setCode("status");
        oldDict.setFrozen(FrozenEnumm.UN_FROZEN);

        DictItemEntity retainedItem = new DictItemEntity();
        retainedItem.setId(11L);
        retainedItem.setMainId(1L);
        retainedItem.setDCode("enabled");
        DictItemEntity removedItem = new DictItemEntity();
        removedItem.setId(12L);
        removedItem.setMainId(1L);
        removedItem.setDCode("disabled");

        when(this.dictMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(this.dictMapper.selectById(1L)).thenReturn(oldDict);
        when(this.dictItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(retainedItem, removedItem));

        DictAoeBo bo = new DictAoeBo();
        bo.setId(1L);
        bo.setCode("status");
        bo.setName("状态");
        bo.setLines(List.of(this.line(11L, "enabled", "启用"), this.line(null, "pending", "待处理")));

        this.service.edit(bo);

        ArgumentCaptor<DictItemEntity> insertedItem = ArgumentCaptor.forClass(DictItemEntity.class);
        verify(this.dictMapper).updateById(any(DictEntity.class));
        verify(this.dictItemMapper).updateById(any(DictItemEntity.class));
        verify(this.dictItemMapper).insert(insertedItem.capture());
        verify(this.dictItemMapper).deleteById(12L);
        verify(this.dictItemMapper, never()).deleteById(11L);
        verify(this.dictCacheHelper, never()).remove(any(String.class));
        assertEquals(1L, insertedItem.getValue().getMainId());
        assertEquals("pending", insertedItem.getValue().getDCode());
        assertTrue(insertedItem.getValue().getId() > 0L);
        assertNotEquals(11L, insertedItem.getValue().getId());
        assertNotEquals(12L, insertedItem.getValue().getId());
        assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
    }

    /**
     * 验证事务提交后才刷新受影响的字典缓存。
     */
    @Test
    void shouldReloadCacheOnlyAfterCommit() {
        DictEntity oldDict = new DictEntity();
        oldDict.setId(1L);
        oldDict.setCode("old-status");
        oldDict.setFrozen(FrozenEnumm.UN_FROZEN);
        when(this.dictMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(this.dictMapper.selectById(1L)).thenReturn(oldDict);
        when(this.dictItemMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        DictAoeBo bo = new DictAoeBo();
        bo.setId(1L);
        bo.setCode("new-status");
        bo.setName("新状态");
        this.service.edit(bo);

        verify(this.dictCacheHelper, never()).remove(any(String.class));
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        verify(this.dictCacheHelper).remove("old-status");
        verify(this.dictCacheHelper).remove("new-status");
    }

    /**
     * 验证事务回滚时不刷新字典缓存。
     */
    @Test
    void shouldNotReloadCacheAfterRollback() {
        DictEntity oldDict = new DictEntity();
        oldDict.setId(1L);
        oldDict.setCode("status");
        oldDict.setFrozen(FrozenEnumm.UN_FROZEN);
        when(this.dictMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(this.dictMapper.selectById(1L)).thenReturn(oldDict);
        when(this.dictItemMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        DictAoeBo bo = new DictAoeBo();
        bo.setId(1L);
        bo.setCode("status");
        bo.setName("状态");
        this.service.edit(bo);

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        }

        verify(this.dictCacheHelper, never()).remove(any(String.class));
    }

    /**
     * 验证提交空明细时删除全部原有明细。
     */
    @Test
    void shouldDeleteAllItemsWhenLinesAreEmpty() {
        DictEntity oldDict = new DictEntity();
        oldDict.setId(1L);
        oldDict.setCode("status");
        oldDict.setFrozen(FrozenEnumm.UN_FROZEN);
        DictItemEntity firstItem = new DictItemEntity();
        firstItem.setId(11L);
        DictItemEntity secondItem = new DictItemEntity();
        secondItem.setId(12L);
        when(this.dictMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(this.dictMapper.selectById(1L)).thenReturn(oldDict);
        when(this.dictItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(firstItem, secondItem));

        DictAoeBo bo = new DictAoeBo();
        bo.setId(1L);
        bo.setCode("status");
        bo.setName("状态");
        bo.setLines(Collections.emptyList());
        this.service.edit(bo);

        verify(this.dictItemMapper).deleteById(11L);
        verify(this.dictItemMapper).deleteById(12L);
        verify(this.dictItemMapper, never()).insert(any(DictItemEntity.class));
        verify(this.dictItemMapper, never()).updateById(any(DictItemEntity.class));
    }

    private DictLineBo line(Long id, String code, String value) {
        DictLineBo line = new DictLineBo();
        line.setId(id);
        line.setKeyName(code);
        line.setKeyValue(value);
        return line;
    }
}
