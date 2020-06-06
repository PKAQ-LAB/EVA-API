package io.nerv.config

import io.nerv.core.license.LicenseCheckInterceptor
import io.nerv.properties.EvaConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.charset.Charset

@Configuration
@ConditionalOnProperty(prefix = "eva.license", name = ["enable"], havingValue = "true")
open class WebConfigurer : WebMvcConfigurer {
    @Autowired
    private val licenseCheckInterceptor: LicenseCheckInterceptor? = null

    @Autowired
    private val evaConfig: EvaConfig? = null

    /**
     * 配置license鉴权拦截器
     * @param registry
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(licenseCheckInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(*evaConfig!!.security!!.permit)
    }

    // 解决乱码问题 StringHttpMessageConverter默认编码为ISO-8859-1
    @Bean
    open fun responseBodyConverter(): HttpMessageConverter<String> {
        return StringHttpMessageConverter(Charset.forName("UTF-8"))
    }
}