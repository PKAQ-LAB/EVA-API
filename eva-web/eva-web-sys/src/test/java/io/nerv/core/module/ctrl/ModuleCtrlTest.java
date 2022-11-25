package io.nerv.core.module.ctrl;

import io.nerv.core.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 模块管理测试类
 *
 * @author: S.PKAQ
 * @Datetime: 2018/4/19 23:40
 */
public class ModuleCtrlTest extends BaseTest {

    @Test
    public void listModule() {
        try {
            mockMvc.perform(get("/module/list"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listModuleByAttr() {
        try {
            mockMvc.perform(get("/module/listModuleByAttr?name='统合部'"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getModule() {
        try {
            mockMvc.perform(get("/module/get/6"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void delModule() {
        try {
            String json = "{param:['34d31e857b7d4a4f94aba2f7061b6058']}";
            mockMvc.perform(post("/module/del")
                            .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void editModule() {
        try {
            String json = "{name: 'junit module code', status: '0000'}";
            mockMvc.perform(post("/module/edit")
                            .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sortModule() {
        try {
            String json = "[{id: '23', orders: 10010}," +
                    "{id: '24', orders: 10086}]";
            mockMvc.perform(post("/module/sort")
                            .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void switchStatus() {
        try {
            String json = "{id:'24',status: '0000'}";
            mockMvc.perform(post("/module/switchStatus")
                            .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}