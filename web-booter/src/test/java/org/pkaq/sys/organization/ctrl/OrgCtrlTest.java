package org.pkaq.sys.organization.ctrl;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.BaseTest;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrgCtrlTest extends BaseTest {
    @Test
    void listAndGetOrganization() throws Exception {
        mockMvc.perform(get("/sys/organization/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").isArray());
        mockMvc.perform(get("/sys/organization/get/{id}", 1000000000000000002L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").exists());
    }

    @Test
    void checkUniqueAcceptsIdPidAndCodeContract() throws Exception {
        mockMvc.perform(post("/sys/organization/checkUnique")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":0,"pid":0,"code":"UNIQUE_ORG_FOR_TEST"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true));
    }
}
