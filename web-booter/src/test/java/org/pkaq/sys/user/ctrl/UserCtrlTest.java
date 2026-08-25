package org.pkaq.sys.user.ctrl;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserCtrlTest extends BaseTest {
    @Test
    void listAndGetUser() throws Exception {
        mockMvc.perform(get("/sys/account/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").exists());
        mockMvc.perform(get("/sys/account/get/{id}", 1000000000000000005L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").exists());
    }
}