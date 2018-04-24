package org.pkaq;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动类
 * @author PKAQ
 */
@SpringBootApplication
@ComponentScan(basePackages = {"org.pkaq.*"})
public class AuthBooter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("  --- --- --- [ auth started ] --- --- ---  ");
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthBooter.class, args);
    }
}
