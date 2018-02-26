package org.pkaq;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"org.pkaq.*"})
public class Booter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("  --- --- --- [ web started ] --- --- ---  ");
    }

    public static void main(String[] args) {
        SpringApplication.run(Booter.class, args);
    }
}
