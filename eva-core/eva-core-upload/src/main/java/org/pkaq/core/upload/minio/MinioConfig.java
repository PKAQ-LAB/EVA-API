package org.pkaq.core.upload.minio;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.properties.File;
import org.pkaq.core.upload.condition.MinIOCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author PKAQ
 */
@Configuration
@RequiredArgsConstructor
@Conditional(MinIOCondition.class)
public class MinioConfig {
    private final EvaConfig evaConfig;

    /**
     * 初始化MinioClient和存储桶
     */
    @Bean
    public MinioClient minioClient() {
        File upload = evaConfig.getUpload();
        if (!StringUtils.hasText(upload.getMinIo().getUrl())) {
            throw new BizException("请配置eva.file.minio.minio_url");
        }
        if (!StringUtils.hasText(upload.getMinIo().getAccess())) {
            throw new BizException("请配置eva.file.minio.minio_access");
        }
        if (!StringUtils.hasText(upload.getMinIo().getSecret())) {
            throw new BizException("请配置eva.file.minio.minio_secret");
        }

        //初始化MinioClient
        return MinioClient.builder()
                .endpoint(upload.getMinIo().getUrl())
                .credentials(upload.getMinIo().getAccess(), upload.getMinIo().getAccess())
                .build();
    }

}
