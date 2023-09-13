package tech.yunyue.core.upload.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import tech.yunyue.core.exception.BizException;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.properties.Upload;
import tech.yunyue.core.upload.condition.MinIOCondition;

import java.util.Objects;

@Conditional(MinIOCondition.class)
@Configuration
public class MinIOConfig {
    /**
     * 创建MinioClient
     */
    @Bean
    public MinioClient minioClient(EvaConfig evaConfig) {
        Upload.MinIO minIo = evaConfig.getUpload().getMinIo();
        if (Objects.isNull(minIo)) {
            throw new BizException("请配置eva.upload.minio");
        }
        if (!StringUtils.hasText(minIo.getUrl())) {
            throw new BizException("请配置eva.upload.minio.url");
        }
        if (!StringUtils.hasText(minIo.getAccess())) {
            throw new BizException("请配置eva.upload.minio.access");
        }
        if (!StringUtils.hasText(minIo.getSecret())) {
            throw new BizException("请配置eva.upload.minio.secret");
        }
        //初始化MinioClient
        return MinioClient.builder()
                .endpoint(minIo.getUrl())
                .credentials(minIo.getAccess(), minIo.getSecret())
                .build();
    }
}
