package org.pkaq.core.upload.minio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.upload.condition.MinIOCondition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author PKAQ
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
@Conditional(MinIOCondition.class)
public class MinioInit implements CommandLineRunner {
    private static final String TEMP = "_temp";
    private static final String STORAGE = "_storage";
    private static final String THUMBNAIL_NAME = "thumbnail_";
    private static final String DRAFT = "draft";
    private final MinIOFileUtil minIOFileUtil;

    /**
     * 初始化存储桶
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        minIOFileUtil.createBucket(TEMP);
        minIOFileUtil.createBucket(STORAGE);
        minIOFileUtil.createBucket(DRAFT);
        minIOFileUtil.createBucket(THUMBNAIL_NAME);

        log.info("------------------ Minio 桶初始化完成 [order - 3]------------------ ");

    }
}
