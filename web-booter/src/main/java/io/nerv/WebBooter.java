package io.nerv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动类
 *
 * @author PKAQ
 */
@Slf4j
@EnableCaching
@SpringBootApplication
@AllArgsConstructor
@ComponentScan(basePackages = {"io.nerv.*"})
public class WebBooter implements CommandLineRunner {
//    private final DictService dictService;

    public static void main(String[] args) {
        SpringApplication.run(WebBooter.class, args);
    }

    @Override
    public void run(String... args) {
//        dictService.init();
        log.info(" ---- WEB BOOTER STARTED ---- ");
    }
}
