package io.nerv;

import io.nerv.core.license.LicenseVerify;
import io.nerv.properties.EvaConfig;
import io.nerv.web.sys.dict.cache.DictCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动类
 * @author PKAQ
 */
@Slf4j
@EnableCaching
@SpringBootApplication
@ComponentScan(basePackages = {"io.nerv.*"})
public class WebBooter implements CommandLineRunner {

    @Autowired
    private EvaConfig evaConfig;

    @Autowired(required = false)
    private LicenseVerify licenseVerify;

    @Autowired
    private DictCacheHelper dictCacheHelper;

    @Override
    public void run(String... args) {
        log.info(" ---- 字典初始化 开始 ---- ");
        this.dictCacheHelper.init();
        this.dictCacheHelper.getAll();
        log.info(" ---- 字典初始化 结束 ---- ");
        if (evaConfig.getLicense().isEnable()){
            // 安装license
            licenseVerify.init();

            // 验证license
            if (!licenseVerify.vertify()) {
                log.error("授权验证未通过, 请更新授权文件");
                Runtime.getRuntime().halt(1);
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(WebBooter.class, args);
    }
}
