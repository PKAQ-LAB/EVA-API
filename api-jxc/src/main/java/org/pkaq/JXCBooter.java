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
public class JXCBooter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("  --- --- --- [ JXC started ] --- --- ---  ");
    }

    public static void main(String[] args) {
        SpringApplication.run(JXCBooter.class, args);
    }
}
