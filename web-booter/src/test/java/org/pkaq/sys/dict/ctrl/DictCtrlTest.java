package org.pkaq.sys.dict.ctrl;

import org.junit.jupiter.api.Test;
import org.pkaq.sys.BaseTest;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class DictCtrlTest extends BaseTest {

    @Test
    public void listDict() {
        try {
            mockMvc.perform(get("/sys/dictionary/list"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void getDict() {
        try {
            mockMvc.perform(get("/dict/get/type/code"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkUnique() {
        try {
            String json = "{code:'biz'}";
            mockMvc.perform(post("/dict/checkUnique")
                            .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void delDict() {
        try {
            mockMvc.perform(get("/dict/del/baf9953cff6a4a16b84ad442e4458d66"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void editDict() {
        try {
            String json = "{name: 'junit dict name', code: 'junit dict code 0000', parentId: 'x'}";
            mockMvc.perform(post("/dict/edit")
                            .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
