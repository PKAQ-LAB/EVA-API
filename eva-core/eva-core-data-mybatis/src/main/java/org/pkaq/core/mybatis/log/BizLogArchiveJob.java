package org.pkaq.core.mybatis.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.mybatis.log.mapper.BusinessLogMapper;
import org.pkaq.core.properties.BizLog;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.DatePatterns;
import org.pkaq.core.util.DateUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Calendar;

/**
 * PostgreSQL 业务日志冷热归档任务。
 *
 * @author PKAQ
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eva.bizlog.archive", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BizLogArchiveJob {
    private final BusinessLogMapper logMapper;
    private final EvaConfig evaConfig;

    /**
     * 将超过配置保留月数的热日志迁移到归档表。
     */
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "${eva.bizlog.archive.cron:0 20 2 * * *}",
            zone = "${eva.bizlog.archive.zone:Asia/Shanghai}")
    public void archive() {
        BizLog.Archive archive = evaConfig.getBizlog().getArchive();
        int afterMonths = Math.max(1, archive.getAfterMonths());
        int batchSize = Math.max(1, archive.getBatchSize());
        int maxBatches = Math.max(1, archive.getMaxBatches());
        if (!logMapper.tryArchiveLock()) {
            log.debug("业务日志归档任务已由其他实例执行");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -afterMonths);
        Date cutoff = calendar.getTime();
        String cutoffText = DateUtils.format(cutoff, DatePatterns.NORM_DATETIME_PATTERN);
        int total = 0;
        for (int batch = 0; batch < maxBatches; batch++) {
            int archived = logMapper.archiveBefore(cutoffText, batchSize);
            total += archived;
            if (archived < batchSize) {
                break;
            }
        }
        log.info("业务日志归档完成, cutoff: {}, archived: {}", cutoffText, total);
    }
}
