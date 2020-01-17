package io.nerv.user.ctrl;

import org.junit.Test;
import io.nerv.BaseTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 用户管理单元测试
 * @author: S.PKAQ
 * @Datetime: 2018/4/18 7:04
 */
public class UserCtrlTest extends BaseTest{

    @Test
    public void checkUnique() {
        try {
            String json = "{account: 'admin'}";
            mockMvc.perform(post("/account/checkUnique")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    public void listUser() {
        try {
            mockMvc.perform(get("/account/list"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getUser() {
        try {
            mockMvc.perform(get("/account/get/{id}","4e51e4cb519f4df29c39bae540607362"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void saveUser() {
        try {
            String json = "{account: 'admin',code: 'admin', name: '超级管理员', password: 'admin'}";
            mockMvc.perform(post("/account/save")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void delUser() {
        try {
            String json = "{param: ['edb26e49c7db4072b2db61d54cd420bf']}";
            mockMvc.perform(post("/account/del")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void lockSwitch() {
        try {
            String json = "{param: ['4e51e4cb519f4df29c39bae540607362'], status: 1}";
            mockMvc.perform(post("/account/lock")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}