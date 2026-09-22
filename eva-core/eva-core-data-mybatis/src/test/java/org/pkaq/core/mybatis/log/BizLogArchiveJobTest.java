package org.pkaq.core.mybatis.log;

import org.junit.jupiter.api.Test;
import org.pkaq.core.mybatis.log.mapper.BusinessLogMapper;
import org.pkaq.core.properties.BizLog;
import org.pkaq.core.properties.EvaConfig;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BizLogArchiveJobTest {

    @Test
    void skipsWhenAnotherInstanceOwnsArchiveLock() {
        BusinessLogMapper mapper = mock(BusinessLogMapper.class);
        EvaConfig config = archiveConfig();
        when(mapper.tryArchiveLock()).thenReturn(false);

        new BizLogArchiveJob(mapper, config).archive();

        verify(mapper, never()).archiveBefore(anyString(), eq(10));
    }

    @Test
    void archivesInStableBatchesUntilLastPartialBatch() {
        BusinessLogMapper mapper = mock(BusinessLogMapper.class);
        EvaConfig config = archiveConfig();
        when(mapper.tryArchiveLock()).thenReturn(true);
        when(mapper.archiveBefore(anyString(), eq(10))).thenReturn(10, 3);

        new BizLogArchiveJob(mapper, config).archive();

        verify(mapper, org.mockito.Mockito.times(2)).archiveBefore(anyString(), eq(10));
    }

    private EvaConfig archiveConfig() {
        BizLog.Archive archive = new BizLog.Archive();
        archive.setAfterMonths(6);
        archive.setBatchSize(10);
        archive.setMaxBatches(3);
        BizLog bizLog = new BizLog();
        bizLog.setArchive(archive);
        EvaConfig config = new EvaConfig();
        config.setBizlog(bizLog);
        return config;
    }
}
