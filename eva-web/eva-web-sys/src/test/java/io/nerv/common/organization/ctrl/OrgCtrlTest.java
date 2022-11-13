package io.nerv.common.organization.ctrl;

import io.nerv.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 组织管理单元测试
 * @author: S.PKAQ
 * @Datetime: 2018/4/19 23:40
 */
public class OrgCtrlTest extends BaseTest {

    @Test
    public void listOrg() {
        try {
            mockMvc.perform(get("/organization/list"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listOrgByAttr() {
        try {
            mockMvc.perform(get("/organization/listOrgByAttr?name='统合部'"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getOrg() {
        try {
            mockMvc.perform(get("/organization/get/6"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void delOrg() {
        try {
            String json = "{param:['b0a14a478c6a493da909acf523cc4768']}";
            mockMvc.perform(post("/organization/del")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void editOrg() {
        try {
            String json = "{name: 'junit org name', status: '0000'}";
            mockMvc.perform(post("/organization/edit")
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
    public void sortOrg() {
        try {
            String json = "[{id: '8', orders: 10010}," +
                    "{id: '9', orders: 10086}]";
            mockMvc.perform(post("/organization/sort")
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
            String json = "{id:'fc79f82f03d94e6f8b1dfa5baf68e95a',status: '0000'}";
            mockMvc.perform(post("/organization/switchStatus")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}