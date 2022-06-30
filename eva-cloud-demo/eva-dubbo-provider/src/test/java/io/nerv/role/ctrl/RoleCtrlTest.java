package io.nerv.role.ctrl;

import io.nerv.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 角色管理单元测试
 * @author: S.PKAQ
 * @Datetime: 2018/4/18 7:04
 */
public class RoleCtrlTest extends BaseTest {

    @Test
    public void checkUnique() {
        try {
            String json = "{code: 'admin'}";
            mockMvc.perform(post("/role/checkUnique")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getRole() {
        try {
            mockMvc.perform(get("/role/get/{id}","1"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listRole() {
        try {
            mockMvc.perform(get("/role/list"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").exists())
                .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listUser() {
        try {
            mockMvc.perform(get("/role/listUser?roleId=1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").exists())
                .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void listModule() {
        try {
            mockMvc.perform(get("/role/listModule?roleId=1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").exists())
                .andReturn().getResponse().getContentAsString();
         } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void saveModule() {
        try {
            String json = "{id: 'cd4105d677434a08a18d3663042ed4f2', modules: [{moduleId:'1'},{moduleId:'2'}]}";
            mockMvc.perform(post("/role/saveModule")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void saveUser() {
        try {
            String json = "{id: 'cd4105d677434a08a18d3663042ed4f2', users: []}";
            mockMvc.perform(post("/role/saveUser")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void saveRole() {
        try {
            String json = "{code: 'junit role code', name: 'junit role name'}";
            mockMvc.perform(post("/role/save")
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
    public void delRole() {
        try {
            String json = "{param: ['cd4105d677434a08a18d3663042ed4f2']}";
            mockMvc.perform(post("/role/del")
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
    public void lockSwitch() {
        try {
            String json = "{param: ['cd4105d677434a08a18d3663042ed4f2'], status: true}";
            mockMvc.perform(post("/role/lock")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").isBoolean())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}