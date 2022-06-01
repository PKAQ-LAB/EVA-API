package io.nerv;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * 单元测试基类
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 8:32
 */
@AutoConfigureMockMvc
@SpringBootTest
public class BaseTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected WebApplicationContext wac;

    @BeforeEach
    public void setupMockMvc(){
        //初始化MockMvc对象
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
}
