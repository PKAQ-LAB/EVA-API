package org.pkaq.sys.notice;

import org.junit.jupiter.api.Test;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.sys.notice.bo.NoticeAoeBo;
import org.pkaq.sys.notice.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 通知模块 PostgreSQL 增删改查及状态联动集成测试。
 *
 * @author PKAQ
 */
@SpringBootTest
@Transactional
class NoticeIntegrationTest {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void completesCrudAndFrozenStateLinkage() {
        String title = "notice-integration-" + Math.abs(System.nanoTime());
        NoticeAoeBo createBo = new NoticeAoeBo();
        createBo.setTitle(title);
        createBo.setContent("create");
        createBo.setType("SYSTEM");
        noticeService.edit(createBo);

        Long noticeId = jdbcTemplate.queryForObject(
                "SELECT ID FROM SYS_NOTICE WHERE TITLE = ?", Long.class, title);
        assertNotNull(noticeId);
        assertEquals("create", noticeService.get(noticeId).getContent());

        NoticeAoeBo updateBo = new NoticeAoeBo();
        updateBo.setId(noticeId);
        updateBo.setTitle(title);
        updateBo.setContent("update");
        updateBo.setType("SYSTEM");
        noticeService.edit(updateBo);
        assertEquals("update", noticeService.get(noticeId).getContent());

        SingleArray<Long> ids = new SingleArray<>();
        ids.setParam(Set.of(noticeId));
        noticeService.switchFrozen(ids);
        assertEquals(1, noticeService.get(noticeId).getFrozen().getCode());

        noticeService.delete(Set.of(noticeId));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SYS_NOTICE WHERE ID = ? AND DELETED = 0", Integer.class, noticeId));
    }
}
