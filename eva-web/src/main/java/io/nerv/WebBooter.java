package io.nerv;

import io.nerv.web.sys.dict.cache.DictHelperProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableCaching
@SpringBootApplication
@ComponentScan(basePackages = {"io.nerv.*"})
public class WebBooter implements CommandLineRunner {

    @Autowired
    private DictHelperProvider dictHelperProvider;

    @Override
    public void run(String... args) {
        log.info(" ---- 字典初始化 开始 ---- ");
        dictHelperProvider.init();
        log.info(" ---- 字典初始化 结束 ---- ");
        System.out.println("  --- --- --- [ web started ] --- --- ---  ");
    }

    public static void main(String[] args) {
        SpringApplication.run(WebBooter.class, args);
    }
    /**
     * 用于接受 shutdown 事件
     */
    @Bean
    public GracefulShutdown gracefulShutdown() {
        return new GracefulShutdown();
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(gracefulShutdown());
        return tomcat;
    }

    /**
     * 优雅关闭 Spring Boot
     * http://www.spring4all.com/article/1022
     */
    private class GracefulShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {
        private final Logger log = LoggerFactory.getLogger(GracefulShutdown.class);
        private volatile Connector connector;
        private final int waitTime = 30;
        @Override
        public void customize(Connector connector) {
            this.connector = connector;
        }
        @Override
        public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
            this.connector.pause();
            Executor executor = this.connector.getProtocolHandler().getExecutor();
            if (executor instanceof ThreadPoolExecutor) {
                try {
                    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                    threadPoolExecutor.shutdown();
                    if (!threadPoolExecutor.awaitTermination(waitTime, TimeUnit.SECONDS)) {
                        log.warn("Tomcat thread pool did not shut down gracefully within " + waitTime + " seconds. Proceeding with forceful shutdown");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
