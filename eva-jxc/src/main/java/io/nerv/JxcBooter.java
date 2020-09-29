package io.nerv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableCaching
@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"io.nerv.*"})
public class JxcBooter implements CommandLineRunner {


    @Override
    public void run(String... args) {
        log.info(" ---- JXC STARTED ---- ");
    }

    public static void main(String[] args) {
        SpringApplication.run(JxcBooter.class, args);
    }
}
