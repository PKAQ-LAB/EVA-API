package io.nerv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;


/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@SpringBootApplication
public class GatewayBooter implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info(" ------ eva cloud gateway  started ------ ");
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayBooter.class, args);
    }

    @Bean("ipKeyResolver")
    public KeyResolver ipKeyResolver(){
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress().getHostString();
            log.info("Ip is : " + ip);
            return Mono.just(ip);
        };
    }
}
