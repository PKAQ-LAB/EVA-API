package io.nerv.core.bizlog.config;

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
     * 是否启用
     */
    private boolean  enabled = false;
    /**
     * 实现类
     */
    private String impl = "";

}
