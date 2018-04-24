package org.pkaq.auth;


import org.junit.Test;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/4/24 7:48
 */
public class AuthCtrlTest extends BaseTest {
    @Test
    public void loginTest() {
        try {
            String json = "{account: 'admin', password: 'admin'}";
            mockMvc.perform(post("/auth/login")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}