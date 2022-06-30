package io.nerv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@SpringBootApplication
public class ConsumerBooter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info(" ------ eva cloud dubbo consumer started ------ ");
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsumerBooter.class, args);
    }
}
