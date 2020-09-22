package io.nerv.web.sys.user.ctrl;

import org.junit.Test;
import io.nerv.BaseTest;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthCtrlTest extends BaseTest {
    @Test
    public void testLogin(){
        try {
            String json = "{account: 'admin', password: '0192023a7bbd73250516f069df18b500'}";
            this.mockMvc.perform(post("/auth/login")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}