package io.nerv;

import io.nerv.web.sys.dict.cache.DictCacheHelper;
import io.nerv.web.sys.dict.service.DictService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableCaching
@SpringBootApplication
@AllArgsConstructor
@ComponentScan(basePackages = {"io.nerv.*"})
public class WebBooter implements CommandLineRunner {

    private final DictService dictService;
    private final DictCacheHelper dictCacheHelper;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Override
    public void run(String... args) {
        log.info(" ---- 字典初始化 开始 ---- ");
        var dictMap = dictService.initDictCache();
        dictMap.forEach((k, v) ->
            dictCacheHelper.cachePut(k, v)
        );

        System.out.println(bCryptPasswordEncoder.encode("0192023a7bbd73250516f069df18b500"));
        log.info(" ---- 字典初始化 结束 ---- ");
        log.info(" ---- WEB BOOTER STARTED ---- ");
    }

    public static void main(String[] args) {
        SpringApplication.run(WebBooter.class, args);
    }
}
