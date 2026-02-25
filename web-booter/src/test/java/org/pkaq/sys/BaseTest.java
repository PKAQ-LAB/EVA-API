package org.pkaq.sys;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * 单元测试基类
 *
 * @author PKAQ
 */
@SpringBootTest
@AutoConfigureMockMvc
public class BaseTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected WebApplicationContext wac;

    @BeforeEach
    public void setupMockMvc() {
        //初始化MockMvc对象
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
}
