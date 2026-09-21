package org.pkaq.sys.notice.ctrl;

import org.junit.jupiter.api.Test;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.notice.service.NoticeService;
import org.pkaq.sys.notice.vo.NoticeVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 通知控制器测试。 @author PKAQ */
class NoticeCtrlTest {
    @Test
    void compatibilityEndpointReturnsStructuredList() {
        NoticeService service = mock(NoticeService.class);
        NoticeVo notice = new NoticeVo();
        notice.setTitle("系统通知");
        when(service.listAvailable()).thenReturn(List.of(notice));

        Response<List<NoticeVo>> response = new NoticeCtrl(service).notices();

        assertEquals(1, response.getData().size());
        assertSame(notice, response.getData().getFirst());
    }
}
