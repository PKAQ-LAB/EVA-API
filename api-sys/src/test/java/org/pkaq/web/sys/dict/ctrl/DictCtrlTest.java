package org.pkaq.web.sys.dict.ctrl;

import org.junit.Test;
import org.pkaq.BaseTest;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 字典管理单元测试
 * @author: S.PKAQ
 * @Datetime: 2018/4/19 23:41
 */
public class DictCtrlTest extends BaseTest {

    @Test
    public void listDict() {
        try {
            mockMvc.perform(get("/dict/list"))
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