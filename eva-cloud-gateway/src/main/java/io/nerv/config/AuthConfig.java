package io.nerv.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 业务日志配置读取类
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 21:06
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "eva.security")
@EnableConfigurationProperties(AuthConfig.class)
public class AuthConfig {
    // 无需鉴权路径(无条件访问)
    private String[] anonymous;

    // 无需资源鉴权的路径
    private String[] permit;
}