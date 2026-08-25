package org.pkaq.sys.dict.ctrl;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DictCtrlTest extends BaseTest {
    @Test
    void listAndQueryDictionary() throws Exception {
        mockMvc.perform(get("/sys/dictionary/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").isArray());
    }
}