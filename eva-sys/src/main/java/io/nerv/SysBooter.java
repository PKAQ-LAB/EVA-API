package io.nerv;

import io.nerv.web.sys.dict.cache.DictCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableCaching
@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"io.nerv.*"})
public class SysBooter implements CommandLineRunner {

    @Autowired
    private DictCacheHelper dictCacheHelper;

    @Override
    public void run(String... args) {
        log.info(" ---- 字典初始化 开始 ---- ");
        this.dictCacheHelper.init();
        this.dictCacheHelper.getAll();
        log.info(" ---- 字典初始化 结束 ---- ");
    }

    public static void main(String[] args) {
        SpringApplication.run(SysBooter.class, args);
    }
}
