package io.nerv;

import io.nerv.biz.sys.dict.service.DictService;
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
    private final DictService dictService;

    @Override
    public void run(String... args) {
        dictService.init();
        log.info(" ---- WEB BOOTER STARTED ---- ");
    }

    public static void main(String[] args) {
        SpringApplication.run(WebBooter.class, args);
    }
}
