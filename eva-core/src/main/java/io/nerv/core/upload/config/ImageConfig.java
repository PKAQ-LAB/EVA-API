package io.nerv.core.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "upload")
@EnableConfigurationProperties(ImageConfig.class)
public class ImageConfig {

    // 图片上传存储临时路径
    private String tempPath;

    // 图片上传存储路径
    private String storagePath;

    // 图片后缀名集
    private String allowSuffixName;

}
