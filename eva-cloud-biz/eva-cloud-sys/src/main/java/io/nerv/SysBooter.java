package io.nerv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableWebMvc
@SpringBootApplication
//Servlet、Filter、Listener 可以直接通过 @WebServlet、@WebFilter、@WebListener 注解自动注册，无需其他代码
@ServletComponentScan
public class SysBooter implements CommandLineRunner {
    private final Environment environment;

    @Value("${server.port}")
    private String ct;

    public SysBooter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        log.info(environment.getProperty("norepeat-check"));
        log.info(environment.getProperty("server.port"));
        log.info(environment.getProperty("eva.cache.type"));
        log.info(" ------ "+ct+" ------ ");
        log.info(" ------ eva cloud System Management started ------ ");
    }

    public static void main(String[] args) {
        SpringApplication.run(SysBooter.class, args);
    }
}
