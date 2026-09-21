package org.pkaq.sys.notice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.sys.notice.bo.NoticeAoeBo;
import org.pkaq.sys.notice.entity.NoticeEntity;
import org.pkaq.sys.notice.mapper.NoticeMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 通知服务测试。 @author PKAQ */
@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {
    @Mock
    private NoticeMapper noticeMapper;

    @Test
    void returnsStructuredAvailableNotices() {
        NoticeEntity entity = new NoticeEntity();
        entity.setId(1L);
        entity.setFrozen(FrozenEnumm.UN_FROZEN);
        entity.setTitle("系统通知");
        entity.setType("notification");
        when(this.noticeMapper.selectList(any())).thenReturn(List.of(entity));

        List<org.pkaq.sys.notice.vo.NoticeVo> result = new NoticeService(this.noticeMapper).listAvailable();

        assertEquals(1, result.size());
        assertEquals("系统通知", result.getFirst().getTitle());
    }

    @Test
    void createsNoticeWithDefaultPublishTimeAndState() {
        NoticeAoeBo bo = new NoticeAoeBo();
        bo.setTitle("系统通知");
        bo.setType("notification");
        NoticeService service = new NoticeService(this.noticeMapper);

        service.edit(bo);

        ArgumentCaptor<NoticeEntity> captor = ArgumentCaptor.forClass(NoticeEntity.class);
        verify(this.noticeMapper).insertOrUpdate(captor.capture());
        assertEquals(FrozenEnumm.UN_FROZEN, captor.getValue().getFrozen());
        assertNotNull(captor.getValue().getDatetime());
    }
}
