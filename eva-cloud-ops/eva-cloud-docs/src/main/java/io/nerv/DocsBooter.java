package io.nerv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@SpringBootApplication
public class DocsBooter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("-------------------------- || EVA KNIFE4J API DOCS STARTED || --------------------------");
    }


    public static void main(String[] args) {
        SpringApplication.run(DocsBooter.class, args);
    }
}
