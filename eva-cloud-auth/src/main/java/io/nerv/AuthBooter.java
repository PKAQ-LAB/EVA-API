package io.nerv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@SpringBootApplication
public class AuthBooter implements CommandLineRunner {
    @Autowired
    private Environment environment;
    @Override
    public void run(String... args) {
        System.out.println(environment.getActiveProfiles());
        log.info(" ------ eva cloud oauth2 server started ------ ");
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthBooter.class, args);
    }
}
