package org.pkaq.core.properties;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EVA 配置绑定适配器。
 *
 * <p>业务配置模型保持为普通 Java 对象，Spring 仅在该适配器中负责外部配置绑定。</p>
 *
 * @author PKAQ
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public class EvaPropertiesConfiguration {

    /**
     * 将 eva 命名空间绑定为业务配置门面。
     *
     * @return EVA 业务配置
     */
    @Bean
    @ConfigurationProperties(prefix = "eva")
    public EvaConfig evaConfig() {
        return new EvaConfig();
    }

    /**
     * 在配置绑定完成后执行 EVA 配置语义校验。
     *
     * @param evaConfig EVA 业务配置
     * @return 配置校验任务
     */
    @Bean
    public SmartInitializingSingleton evaConfigurationValidation(EvaConfig evaConfig) {
        return () -> EvaConfigurationValidator.validate(evaConfig);
    }
}
