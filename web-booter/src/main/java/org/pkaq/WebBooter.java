package org.pkaq;

import lombok.extern.slf4j.Slf4j;
import org.pkaq.sys.dict.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

/**
 * 启动类
 *
 * @author PKAQ
 */
@Slf4j
@EnableCaching
@SpringBootApplication
@ComponentScan(basePackages = {"org.pkaq.*"})
public class WebBooter implements CommandLineRunner {

    public static void main(String[] args) {
        var application = SpringApplication.run(WebBooter.class, args);

        Environment env = application.getEnvironment();

        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");

        log.info(" ------------------ Swagger ------------------ ");
        log.info(" #                                            #");
        log.info(" # Local: http://localhost:" + port + path + "/doc.html  #");
        log.info(" #                                            #");
        log.info(" ------------------ Swagger ------------------ ");

    }

    @Override
    public void run(String... args) {
        log.info(" ------------------ WEB BOOTER STARTED ------------------ ");
    }
}
