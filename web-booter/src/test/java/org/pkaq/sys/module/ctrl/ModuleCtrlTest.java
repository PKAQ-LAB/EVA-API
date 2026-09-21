package org.pkaq.sys.module.ctrl;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.BaseTest;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ModuleCtrlTest extends BaseTest {
    @Test
    void listAndGetModule() throws Exception {
        mockMvc.perform(get("/sys/module/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").isArray());
    }

    @Test
    void checkUniqueAcceptsIdPidAndCodeContract() throws Exception {
        mockMvc.perform(post("/sys/module/checkUnique")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":0,"pid":0,"code":"UNIQUE_MODULE_FOR_TEST"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true));
    }

    @Test
    void frozenAcceptsExplicitTargetState() throws Exception {
        mockMvc.perform(post("/sys/module/frozen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"param":[1],"frozen":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true));
    }
}
