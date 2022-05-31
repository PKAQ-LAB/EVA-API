package io.nerv;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableAdminServer
@SpringBootApplication
public class SkywalkingBooter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("-------------------------- || EVA BOOT ADMIN STARTED || --------------------------");
    }


    public static void main(String[] args) {
        SpringApplication.run(SkywalkingBooter.class, args);
    }
}
