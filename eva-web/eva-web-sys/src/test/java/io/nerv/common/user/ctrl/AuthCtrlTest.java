package io.nerv.common.user.ctrl;

import io.nerv.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthCtrlTest extends BaseTest {
    @Test
    public void testLogin(){
        try {
                String json = "{account: 'admin', password: '123456'}";
            this.mockMvc.perform(post("/auth/login")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}