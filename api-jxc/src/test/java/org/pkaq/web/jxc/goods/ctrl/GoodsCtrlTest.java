package org.pkaq.web.jxc.goods.ctrl;

import org.junit.Test;
import org.pkaq.web.BaseTest;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 商品信息维护单元测试
 * @author: S.PKAQ
 * @Datetime: 2018/10/14 10:06
 */
public class GoodsCtrlTest extends BaseTest {

    @Test
    public void testSave(){
        try {
            String json = "{name: '测试品名-BRAVO', category: '0000', model: '7130',mnemonic: 'atmbsq',barcode:'62391232',unit:'0004',boxunit: 10, productor:'A123232'}";
            this.mockMvc.perform(post("/goods/edit")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEdit(){
        try {
            String json = "{id:'1051305116113580033', name: '测试品名修改', category: '0000', model: '7130',mnemonic: 'atmbsq',barcode:'62391232',unit:'0004',boxunit: 10, productor:'A123232'}";
            this.mockMvc.perform(post("/goods/edit")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDel(){
        try {
            String json = "{ids: ['1051305116113580033']}";
            this.mockMvc.perform(post("/goods/del")
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
    public void testGet(){
        try {
            this.mockMvc.perform(get("/goods/get/{id}","1051305116113580033"))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testList(){
        try {
            this.mockMvc.perform(get("/goods/list"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("success").value(true))
                    .andExpect(jsonPath("data").exists())
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}