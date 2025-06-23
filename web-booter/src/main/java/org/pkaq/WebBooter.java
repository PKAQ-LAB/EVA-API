package org.pkaq;

import lombok.extern.slf4j.Slf4j;
import org.pkaq.sys.dict.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

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
    @Autowired
    ApplicationContext ctx;

    public static void main(String[] args) {
        SpringApplication.run(WebBooter.class, args);
    }

    @Override
    public void run(String... args) {
        for (String name : ctx.getBeanNamesForType(IDictService.class)) {
            System.out.println(" - " + name + " : " + ctx.getType(name));
        }

        log.info(" ---- WEB BOOTER STARTED ---- ");
    }
}
