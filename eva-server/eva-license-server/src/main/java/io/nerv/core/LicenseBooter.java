package io.nerv.core;

import io.nerv.core.license.LicenseGenerator;
import io.nerv.core.license.LicenseVerify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动类
 *
 * @author PKAQ
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"io.nerv.*"})
public class LicenseBooter implements CommandLineRunner {

    @Autowired
    private LicenseGenerator licenseGenerator;

    @Autowired
    private LicenseVerify licenseVerify;

    public static void main(String[] args) {
        SpringApplication.run(LicenseBooter.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info(" ---- License Application Started ---- ");
        licenseGenerator.create();
        log.info(" ---- License Application Generated ---- ");
//        licenseVerify.install();
//        if (!licenseVerify.vertify()) {
//            log.error("授权已过期, 请更新授权文件");
//            Runtime.getRuntime().halt(1);
//        }
    }
}
