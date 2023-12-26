package tech.yunyue.core.upload.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.properties.Upload;
import tech.yunyue.core.upload.condition.OssCondition;

/**
 * @author : dmz
 * @date : 2023/12/24
 */
@Conditional(OssCondition.class)
@Configuration
public class OssConfig {
    /**
     * 创建MinioClient
     */
    @Bean
    public OSS minioClient(EvaConfig evaConfig) {
        Upload.Oss ossIo = evaConfig.getUpload().getOss();

        // 初始化ossClient
        return new OSSClientBuilder().build(ossIo.getUrl(), ossIo.getAccess(), ossIo.getSecret());
    }
}
