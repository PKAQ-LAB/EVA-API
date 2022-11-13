package io.nerv;

import io.swagger.v3.core.util.Json;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

import java.util.ArrayList;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableCaching
@SpringBootApplication
@AllArgsConstructor
@ComponentScan(basePackages = {"io.nerv.*"})
public class WebBooter implements CommandLineRunner {
    @Override
    public void run(String... args) {
        log.info(" ---- WEB BOOTER STARTED ---- ");
    }

    public static void main(String[] args) {
        SpringApplication.run(WebBooter.class, args);
    }
}
