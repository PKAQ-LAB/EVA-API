package org.pkaq.sys.role.ctrl;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleCtrlTest extends BaseTest {
    @Test
    void listAndFetchRoleResources() throws Exception {
        mockMvc.perform(get("/sys/role/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").exists());
        mockMvc.perform(get("/sys/role/fetchResource")
                        .param("roleId", "1000000000000000004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").exists());
    }
}