package org.pkaq.order;

import org.junit.Test;
import org.pkaq.sys.BaseTest;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 角色管理单元测试
 * @author: S.PKAQ
 * @Datetime: 2018/4/18 7:04
 */
public class OrderCtrlTest extends BaseTest {


    @Test
    public void saveUser() {
        try {
            String json = "{'id': 10086, 'month': '2018-08-10',note: ''}";
            mockMvc.perform(post("/order/save")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}