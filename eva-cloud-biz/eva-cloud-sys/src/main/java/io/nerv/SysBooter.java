package io.nerv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@SpringBootApplication
//Servlet、Filter、Listener 可以直接通过 @WebServlet、@WebFilter、@WebListener 注解自动注册，无需其他代码
@ServletComponentScan
public class SysBooter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info(" ------ eva cloud System Management started ------ ");
    }

    public static void main(String[] args) {
        SpringApplication.run(SysBooter.class, args);
    }
}
