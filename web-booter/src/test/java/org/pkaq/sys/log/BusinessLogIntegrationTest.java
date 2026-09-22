package org.pkaq.sys.log;

import org.junit.jupiter.api.Test;
import org.pkaq.core.log.base.BusinessLogRepository;
import org.pkaq.core.log.bo.LogQueryBo;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mybatis.log.BizLogArchiveJob;
import org.pkaq.core.mybatis.log.entity.MybatisBizLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 业务日志 PostgreSQL 热数据、归档和统一查询集成测试。
 *
 * @author PKAQ
 */
@SpringBootTest
@Transactional
class BusinessLogIntegrationTest {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BusinessLogRepository businessLogRepository;

    @Autowired
    private BizLogArchiveJob archiveJob;

    @Test
    void queriesHotAndArchivedLogsWithTenantIsolation() {
        long hotId = -Math.abs(System.nanoTime());
        long archivedId = hotId - 1;
        String hotBizId = "integration-hot-" + Math.abs(hotId);
        String archivedBizId = "integration-archive-" + Math.abs(archivedId);
        insertLog(hotId, hotBizId, LocalDateTime.now());
        insertLog(archivedId, archivedBizId, LocalDateTime.now().minusMonths(7));

        LogQueryBo hotQuery = query(hotBizId);
        PageVo<?> hotPage = (PageVo<?>) businessLogRepository.list(hotQuery);
        assertEquals(1, hotPage.getTotal());
        assertTrue(businessLogRepository.get(String.valueOf(hotId)).contains(hotBizId));

        archiveJob.archive();

        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM LOG_BIZ WHERE ID = ?", Integer.class, archivedId));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM LOG_BIZ_ARCHIVE WHERE ID = ?", Integer.class, archivedId));
        PageVo<?> archivePage = (PageVo<?>) businessLogRepository.list(query(archivedBizId));
        assertEquals(1, archivePage.getTotal());
        MybatisBizLogEntity archived = (MybatisBizLogEntity) archivePage.getRecords().getFirst();
        assertEquals(Boolean.TRUE, archived.getArchived());
    }

    private void insertLog(long id, String bizId, LocalDateTime operateDatetime) {
        jdbcTemplate.update("""
                INSERT INTO LOG_BIZ (
                    ID, TENANT_ID, USER_ID, OPERATOR, OPERATE_TYPE, OPERATE_DATETIME,
                    DESCRIPTION, M_CODE, B_ID, SUCCESS
                ) VALUES (?, 0, 42, 'integration', 'U', ?, 'integration test', 'TEST', ?, TRUE)
                """, id, FORMATTER.format(operateDatetime), bizId);
    }

    private LogQueryBo query(String bizId) {
        LogQueryBo query = new LogQueryBo();
        query.setBegin(new Date(0));
        query.setEnd(new Date(System.currentTimeMillis() + 60_000));
        query.setBId(bizId);
        query.setIncludeArchived(Boolean.TRUE);
        return query;
    }
}
