package io.nerv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@SpringBootApplication
public class GatewayBooter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info(" ------ eva cloud gateway started ------ ");
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayBooter.class, args);
    }
}
