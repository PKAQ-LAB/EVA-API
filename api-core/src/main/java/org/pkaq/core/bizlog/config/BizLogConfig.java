package org.pkaq.core.bizlog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 业务日志配置读取类
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 21:06
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bizlog")
public class BizLogConfig {
    /**
     * 实现类
     */
    private String impl = "";

    /**
     * 切入点
     */
    private String basePackage = "";
}
