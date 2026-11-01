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
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.bo.DictLineBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.cache.DictCacheHelper.Owner;
import org.pkaq.core.threaduser.ThreadUser;
import org.pkaq.core.threaduser.ThreadUserHelper;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"key", "value", "sort", "remark", "frozen",
            "omit", "foreign", "duplicate", "ordinary9999", "new9999", "main9999", "unknown", "null", "blank"})
    void rejectsWholeEditBeforeAnyWrite(String change) {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
            DictEntity main = new DictEntity();
            main.setId(1L);
            main.setFrozen(FrozenEnumm.UN_FROZEN);
            DictItemEntity protectedItem = protectedItem();
            org.mockito.Mockito.lenient().when(dictMapper.selectById(1L)).thenReturn(main);
            org.mockito.Mockito.lenient().when(dictItemMapper.selectList(any(Wrapper.class)))
                    .thenReturn(List.of(protectedItem));
            DictLineBo line = this.line(11L, "fixed", "固定");
            DictAoeBo bo = new DictAoeBo();
            bo.setId(1L);
            bo.setCode("status");
            bo.setName("状态");
            bo.setLines(new java.util.ArrayList<>(List.of(this.line(null, "normal", "正常"), line)));
            switch (change) {
                case "key" -> line.setKeyName("changed");
                case "value" -> line.setKeyValue("改变");
                case "sort" -> line.setOrders(8D);
                case "remark" -> line.setRemark("");
                case "frozen" -> line.setFrozen(0);
                case "omit" -> bo.getLines().remove(line);
                case "foreign" -> line.setId(999L);
                case "duplicate" -> bo.getLines().add(line);
                case "ordinary9999" -> {
                    protectedItem.setFrozen(FrozenEnumm.UN_FROZEN);
                    line.setFrozen(9999);
                }
                case "new9999" -> bo.getLines().getFirst().setFrozen(9999);
                case "main9999" -> bo.setFrozen(9999);
                case "unknown" -> bo.getLines().getFirst().setFrozen(2);
                case "null" -> bo.getLines().add(null);
                case "blank" -> line.setKeyValue("");
                default -> throw new AssertionError(change);
            }
            assertThrows(RuntimeException.class, () -> service.edit(bo));
            verify(dictMapper, never()).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
            verify(dictMapper, never()).insertPrivate(eq("eva"), any(DictEntity.class));
            verify(dictItemMapper, never()).updatePrivate(eq("eva"), any(DictItemEntity.class), anyInt());
            verify(dictItemMapper, never()).insertPrivate(eq("eva"), any(DictItemEntity.class));
            verify(dictItemMapper, never()).deletePrivate(eq("eva"), any(), anyInt());
            org.mockito.Mockito.verifyNoInteractions(dictCacheHelper);

        });
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {true, false})
    void unchangedProtectedLineIsRetainedWithoutUpdateAndRemarkReadsBack(boolean explicit) {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
            DictEntity main = new DictEntity();
            main.setId(1L);
            main.setCode("status");
            main.setFrozen(FrozenEnumm.UN_FROZEN);
            main.setRemark("真实备注");
            when(dictMapper.selectById(1L)).thenReturn(main);
            DictAoeBo bo = new DictAoeBo();
            bo.setId(1L);
            bo.setCode("status");
            bo.setName("状态");
            bo.setRemark("真实备注");
            DictLineBo fixed = this.line(11L, "fixed", "固定");
            if (explicit) {
                fixed.setOrders(7D);
                fixed.setFrozen(9999);
            }
            DictItemEntity ordinary = new DictItemEntity();
            ordinary.setId(12L);
            ordinary.setMainId(1L);
            ordinary.setFrozen(FrozenEnumm.UN_FROZEN);
            when(dictItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(protectedItem(), ordinary));
            bo.setLines(List.of(fixed, this.line(12L, "normal", "修改")));
            service.edit(bo);
            verify(dictMapper).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
            ArgumentCaptor<DictItemEntity> updated = ArgumentCaptor.forClass(DictItemEntity.class);
            verify(dictItemMapper).updatePrivate(eq("eva"), updated.capture(), anyInt());
            assertEquals(12L, updated.getValue().getId());
            verify(dictItemMapper, never()).deletePrivate(eq("eva"), any(), anyInt());
            assertEquals("真实备注", service.getDict(bo).getRemark());
            when(dictMapper.selectList(any(Wrapper.class))).thenReturn(List.of(main));
            assertEquals("真实备注", service.listDict().getFirst().getRemark());

        });
    }

    @Test
    void deletingMainWithProtectedLineIsRejected() {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
            DictEntity main = new DictEntity();
            main.setId(1L);
            main.setFrozen(FrozenEnumm.UN_FROZEN);
            when(dictMapper.selectById(1L)).thenReturn(main);
            when(dictItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(protectedItem()));
            assertThrows(RuntimeException.class, () -> service.delDict(1L));
            verify(dictMapper, never()).deletePrivate(eq("eva"), any(), anyInt());
            verify(dictItemMapper, never()).delete(any(Wrapper.class));

        });
    }

    private DictItemEntity protectedItem() {
        DictItemEntity item = new DictItemEntity();
        item.setId(11L);
        item.setMainId(1L);
        item.setDCode("fixed");
        item.setDValue("固定");
        item.setSort(7D);
        item.setFrozen(FrozenEnumm.READ_ONLY);
        return item;
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"missing", "readonly", "null", "zero", "negative"})
    void rejectsWholeSwitchBeforeAnyWrite(String invalid) {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
            DictEntity normal = new DictEntity();
            normal.setId(1L);
            normal.setFrozen(FrozenEnumm.UN_FROZEN);
            org.mockito.Mockito.lenient().when(dictMapper.selectById(1L)).thenReturn(normal);
            if ("readonly".equals(invalid)) {
                DictEntity readOnly = new DictEntity();
                readOnly.setFrozen(FrozenEnumm.READ_ONLY);
                when(dictMapper.selectById(2L)).thenReturn(readOnly);
            }
            Long bad = switch (invalid) {
                case "null" -> null;
                case "zero" -> 0L;
                case "negative" -> -1L;
                default -> 2L;
            };
            org.pkaq.core.mvc.bo.SingleArray<Long> ids = new org.pkaq.core.mvc.bo.SingleArray<>();
            java.util.Set<Long> values = new java.util.LinkedHashSet<>();
            values.add(1L);
            values.add(bad);
            ids.setParam(values);
            assertThrows(RuntimeException.class, () -> service.switchFrozen(ids));
            verify(dictMapper, never()).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
            org.mockito.Mockito.verifyNoInteractions(dictCacheHelper);

        });
    }
    @Mock
    private DictMapper dictMapper;

    @Mock
    private DictItemMapper dictItemMapper;

    @Mock
    private DictCacheHelper dictCacheHelper;

    private DictService service;

    @Mock
    private DictItemSnapshot itemSnapshot;

    @Mock
    private DictBusinessAccess businessAccess;

    /**
     * 初始化待测服务和事务同步上下文。
     */
    @BeforeEach
    void setUp() {
        DictConvert convert = Mappers.getMapper(DictConvert.class);
        this.service = new DictService(this.dictCacheHelper, this.dictItemMapper, convert, new EvaConfig(),
                itemSnapshot, businessAccess);
        org.mockito.Mockito.lenient().when(itemSnapshot.load(any(Long.class))).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            DictEntity main = dictMapper.selectById(id);
            if (main == null || main.getFrozen() == FrozenEnumm.READ_ONLY) {
                org.pkaq.sys.SysCodes.READ_ONLY_RECORD.newException();
            }
            List<DictItemEntity> items = dictItemMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DictItemEntity>()
                            .eq(DictItemEntity::getMainId, id));
            return new DictItemSnapshot.Snapshot(main, items.stream()
                    .filter(item -> item.getFrozen() == FrozenEnumm.READ_ONLY).toList());
        });
        ReflectionTestUtils.setField(this.service, "mapper", this.dictMapper);
        org.mockito.Mockito.lenient().when(businessAccess.businessOwner()).thenReturn(new Owner("eva", "BUSINESS"));
        org.mockito.Mockito.lenient().when(businessAccess.sources())
                .thenReturn(List.of(new Owner("eva", "BUSINESS")));
        org.mockito.Mockito.lenient().when(dictMapper.selectVisible(eq("eva"), any(Wrapper.class)))
                .thenAnswer(call -> {
                    var wrapper = (com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?>)
                            call.getArgument(1);
                    wrapper.getSqlSegment();
                    Long id = wrapper.getParamNameValuePairs().values().stream()
                            .filter(Long.class::isInstance).map(Long.class::cast).findFirst().orElse(null);
                    if (id == null) {
                        return dictMapper.selectList((Wrapper<DictEntity>) call.getArgument(1));
                    }
                    DictEntity found = dictMapper.selectById(id);
                    if (found != null && found.getCategory() == null) {
                        found.setCategory("BUSINESS");
                    }
                    return found == null ? List.of() : List.of(found);
                });
        org.mockito.Mockito.lenient().when(dictItemMapper.selectVisible(eq("eva"), any(Wrapper.class)))
                .thenAnswer(call -> dictItemMapper.selectList((Wrapper<DictItemEntity>) call.getArgument(1)));
        org.mockito.Mockito.lenient().when(dictMapper.updatePrivate(eq("eva"), any(), anyInt())).thenReturn(1);
        org.mockito.Mockito.lenient().when(dictMapper.insertPrivate(eq("eva"), any())).thenReturn(1);
        org.mockito.Mockito.lenient().when(dictMapper.deletePrivate(eq("eva"), any(), anyInt())).thenReturn(1);
        org.mockito.Mockito.lenient().when(dictItemMapper.updatePrivate(eq("eva"), any(), anyInt())).thenReturn(1);
        org.mockito.Mockito.lenient().when(dictItemMapper.insertPrivate(eq("eva"), any())).thenReturn(1);
        org.mockito.Mockito.lenient().when(dictItemMapper.deletePrivate(eq("eva"), any(), anyInt())).thenReturn(1);
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    /**
     * 清理事务同步上下文。
     */
    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    /**
     * 验证编辑字典时按差异更新明细，并延迟刷新缓存。
     */
    @Test
    void shouldDiffSaveItemsWhenEditingDict() {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
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

            when(this.dictMapper.selectById(1L)).thenReturn(oldDict);
            when(this.dictItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(retainedItem, removedItem));

            DictAoeBo bo = new DictAoeBo();
            bo.setId(1L);
            bo.setCode("status");
            bo.setName("状态");
            bo.setLines(List.of(this.line(11L, "enabled", "启用"), this.line(null, "pending", "待处理")));

            this.service.edit(bo);

            ArgumentCaptor<DictItemEntity> insertedItem = ArgumentCaptor.forClass(DictItemEntity.class);
            verify(this.dictMapper).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());
            verify(this.dictItemMapper).updatePrivate(eq("eva"), any(DictItemEntity.class), anyInt());
            verify(this.dictItemMapper).insertPrivate(eq("eva"), insertedItem.capture());
            verify(this.dictItemMapper).deletePrivate(eq("eva"), argThat(row -> row.getId().equals(12L)), anyInt());
            verify(this.dictItemMapper, never()).deletePrivate(eq("eva"), argThat(row -> row.getId().equals(11L)),
                    anyInt());
            verify(this.dictCacheHelper, never()).remove(any(Owner.class), any(String.class));
            assertEquals(1L, insertedItem.getValue().getMainId());
            assertEquals("pending", insertedItem.getValue().getDCode());
            assertTrue(insertedItem.getValue().getId() > 0L);
            assertNotEquals(11L, insertedItem.getValue().getId());
            assertNotEquals(12L, insertedItem.getValue().getId());
            assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());

        });
    }

    /**
     * 验证事务提交后才刷新受影响的字典缓存。
     */
    @Test
    void shouldReloadCacheOnlyAfterCommit() {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
            DictEntity oldDict = new DictEntity();
            oldDict.setId(1L);
            oldDict.setCode("old-status");
            oldDict.setFrozen(FrozenEnumm.UN_FROZEN);
            when(this.dictMapper.selectById(1L)).thenReturn(oldDict);
            when(this.dictItemMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

            DictAoeBo bo = new DictAoeBo();
            bo.setId(1L);
            bo.setCode("new-status");
            bo.setName("新状态");
            this.service.edit(bo);

            verify(this.dictCacheHelper, never()).remove(any(Owner.class), any(String.class));
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            verify(this.dictCacheHelper).remove(new Owner("eva", "BUSINESS"), "old-status");
            verify(this.dictCacheHelper).remove(new Owner("eva", "BUSINESS"), "new-status");

        });
    }

    /**
     * 验证事务回滚时不刷新字典缓存。
     */
    @Test
    void shouldNotReloadCacheAfterRollback() {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
            DictEntity oldDict = new DictEntity();
            oldDict.setId(1L);
            oldDict.setCode("status");
            oldDict.setFrozen(FrozenEnumm.UN_FROZEN);
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

            verify(this.dictCacheHelper, never()).remove(any(Owner.class), any(String.class));

        });
    }

    /**
     * 验证提交空明细时删除全部原有明细。
     */
    @Test
    void shouldDeleteAllItemsWhenLinesAreEmpty() {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
            DictEntity oldDict = new DictEntity();
            oldDict.setId(1L);
            oldDict.setCode("status");
            oldDict.setFrozen(FrozenEnumm.UN_FROZEN);
            DictItemEntity firstItem = new DictItemEntity();
            firstItem.setId(11L);
            DictItemEntity secondItem = new DictItemEntity();
            secondItem.setId(12L);
            when(this.dictMapper.selectById(1L)).thenReturn(oldDict);
            when(this.dictItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(firstItem, secondItem));

            DictAoeBo bo = new DictAoeBo();
            bo.setId(1L);
            bo.setCode("status");
            bo.setName("状态");
            bo.setLines(Collections.emptyList());
            this.service.edit(bo);

            verify(this.dictItemMapper).deletePrivate(eq("eva"), argThat(row -> row.getId().equals(11L)), anyInt());
            verify(this.dictItemMapper).deletePrivate(eq("eva"), argThat(row -> row.getId().equals(12L)), anyInt());
            verify(this.dictItemMapper, never()).insertPrivate(eq("eva"), any(DictItemEntity.class));
            verify(this.dictItemMapper, never()).updatePrivate(eq("eva"), any(DictItemEntity.class), anyInt());

        });
    }

    @Test
    void shouldRejectEditingReadOnlyDict() {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
            DictEntity readOnly = new DictEntity();
            readOnly.setId(1L);
            readOnly.setCode("status");
            readOnly.setFrozen(FrozenEnumm.READ_ONLY);
            when(this.dictMapper.selectById(1L)).thenReturn(readOnly);

            DictAoeBo bo = new DictAoeBo();
            bo.setId(1L);
            bo.setCode("status");
            bo.setName("状态");

            assertThrows(RuntimeException.class, () -> this.service.edit(bo));
            verify(this.dictMapper, never()).updatePrivate(eq("eva"), any(DictEntity.class), anyInt());

        });
    }

    @Test
    void shouldRejectDeletingReadOnlyDict() {
        ThreadUserHelper.runWithUser(new ThreadUser().setUserId(7L).setName("synthetic"), () -> {
            DictEntity readOnly = new DictEntity();
            readOnly.setId(1L);
            readOnly.setCode("status");
            readOnly.setFrozen(FrozenEnumm.READ_ONLY);
            when(this.dictMapper.selectById(1L)).thenReturn(readOnly);

            assertThrows(RuntimeException.class, () -> this.service.delDict(1L));
            verify(this.dictMapper, never()).deletePrivate(eq("eva"), argThat(row -> row.getId().equals(1L)), anyInt());
            verify(this.dictItemMapper, never()).delete(any(Wrapper.class));

        });
    }

    private DictLineBo line(Long id, String code, String value) {
        DictLineBo line = new DictLineBo();
        line.setId(id);
        line.setKeyName(code);
        line.setKeyValue(value);
        return line;
    }
}
