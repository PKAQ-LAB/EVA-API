package org.pkaq.config;


import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.List;

/**
 * i18n配置项，内置SYSI18N为系统默认的内置国际化文件
 */
@Configuration
public class I18NConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.messages")
    public MessageSourceProperties messageSourceProperties() {
        return new MessageSourceProperties();
    }

    @Bean
    public MessageSource messageSource(MessageSourceProperties messageSourceProperties) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding(messageSourceProperties.getEncoding().name());

        // 获取 spring.messages.basename 配置的文件路径列表
        List<String> baseNames = messageSourceProperties.getBasename();

        // 追加内置的 SYSI81N 文件
        baseNames.add("i18n/SYSI18N");

        // 设置更新后的 basename 列表
        messageSource.setBasenames(baseNames.toArray(new String[0]));
        messageSource.setUseCodeAsDefaultMessage(messageSourceProperties.isUseCodeAsDefaultMessage());
        return messageSource;
    }
}
