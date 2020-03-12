package io.nerv;

import io.nerv.core.license.LicenseVerify;
import io.nerv.core.upload.util.NgFileUploadUtil;
import io.nerv.properties.EvaConfig;
import io.nerv.server.undertow.GracefulShutdownUndertowWrapper;
import io.nerv.web.sys.dict.cache.DictCacheHelper;
import io.undertow.UndertowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableCaching
@EnableJpaAuditing
@SpringBootApplication
@ComponentScan(basePackages = {"io.nerv.*"})
public class WebBooter implements CommandLineRunner {

    @Autowired
    private EvaConfig evaConfig;

    @Autowired(required = false)
    private LicenseVerify licenseVerify;

    @Autowired
    private DictCacheHelper dictCacheHelper;

    @Autowired(required = false)
    private GracefulShutdownUndertowWrapper gracefulShutdownUndertowWrapper;


    @Autowired
    private NgFileUploadUtil ngFileUploadUtil;

    @Override
    public void run(String... args) {
        log.info(" ---- 字典初始化 开始 ---- ");
        this.dictCacheHelper.init();
        this.dictCacheHelper.getAll();
        log.info(" ---- 字典初始化 结束 ---- ");
        if (evaConfig.getLicense().isEnable()){
            // 安装license
            licenseVerify.init();

            // 验证license
            if (!licenseVerify.vertify()) {
                log.error("授权验证未通过, 请更新授权文件");
                Runtime.getRuntime().halt(1);
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(WebBooter.class, args);
    }

    /**
     * 用于接受 shutdown 事件
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.profiles", name = "active", havingValue = "prod")
    public UndertowServletWebServerFactory servletWebServerFactory() {
        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        factory.addDeploymentInfoCustomizers(deploymentInfo -> deploymentInfo.addOuterHandlerChainWrapper(gracefulShutdownUndertowWrapper));
        factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_STATISTICS, true));
        return factory;
    }
}
