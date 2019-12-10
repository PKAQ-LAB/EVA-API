package io.nerv;

import io.nerv.server.undertow.GracefulShutdownUndertowWrapper;
import io.nerv.web.sys.dict.cache.DictHelperProvider;
import io.undertow.UndertowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
    private DictHelperProvider dictHelperProvider;

    @Autowired
    private GracefulShutdownUndertowWrapper gracefulShutdownUndertowWrapper;

    @Override
    public void run(String... args) {
        log.info(" ---- 字典初始化 开始 ---- ");
        this.dictHelperProvider.init();
        log.info(" ---- 字典初始化 结束 ---- ");
    }


    public static void main(String[] args) {
        SpringApplication.run(WebBooter.class, args);
    }

    /**
     * 用于接受 shutdown 事件
     */
    @Bean
    public UndertowServletWebServerFactory servletWebServerFactory() {
        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        factory.addDeploymentInfoCustomizers(deploymentInfo -> deploymentInfo.addOuterHandlerChainWrapper(gracefulShutdownUndertowWrapper));
        factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_STATISTICS, true));
        return factory;
    }
}
