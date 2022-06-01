package io.nerv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableWebMvc
@SpringBootApplication
public class SysBooter implements CommandLineRunner {
    private final Environment environment;

    public SysBooter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        System.out.println(environment.getActiveProfiles());
        log.info(" ------ eva cloud System Management started ------ ");
    }

    public static void main(String[] args) {
        SpringApplication.run(SysBooter.class, args);
    }
}
