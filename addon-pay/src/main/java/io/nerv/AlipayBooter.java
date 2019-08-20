package io.nerv;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 启动类
 * @author PKAQ
 */
@SpringBootApplication
@EnableConfigurationProperties
public class AlipayBooter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("  --- --- --- [ Alipay started ] --- --- ---  ");
    }

    public static void main(String[] args) {
        SpringApplication.run(AlipayBooter.class, args);
    }
}
