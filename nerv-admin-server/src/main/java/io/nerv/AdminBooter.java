package io.nerv;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import io.undertow.UndertowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.annotation.Bean;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableAdminServer
@SpringBootApplication
public class AdminBooter implements CommandLineRunner {


    @Autowired
    private GracefulShutdownUndertowWrapper gracefulShutdownUndertowWrapper;

    @Override
    public void run(String... args) {
        System.out.println("-------------------------- || EVA BOOT ADMIN STARTED || --------------------------");
    }


    public static void main(String[] args) {
        SpringApplication.run(AdminBooter.class, args);
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
