package org.pkaq.core.upload.minio;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MinIOFileUtilTest {
    @Test
    void rejectsLocalThumbnailOperations() {
        MinIOFileUtil fileUtil = new MinIOFileUtil(mock(MinioClient.class));
        File source = new File("source.png");
        File destination = new File("thumbnail.png");
        assertThrows(UnsupportedOperationException.class, () -> fileUtil.thumbnail(source, 0.5F));
        assertThrows(UnsupportedOperationException.class, () -> fileUtil.thumbnail(source, destination, 0.5F));
    }
}
