package org.pkaq.sys.dict.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.sys.dict.service.IDictService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class DictInit implements CommandLineRunner {
    private final IDictService dictService;
    @Override
    public void run(String... args) {
        dictService.init();
        log.info("------------------ 字典初始化成功 ------------------ ");
    }
}
