package org.pkaq.web.instock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 采购入库单单元测试
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 8:32
 */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class InstockTest{
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setupMockMvc(){
        //初始化MockMvc对象
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    /**
     * 采购入库单新增
     */
    @Test
    public void testSave(){
        try {
            String json = "{incode:'tom','indate':'2018-02-11',remark:'this is remarks',line: [{" +
                    "barcode:'goods_01',name:'商品01',price:18,num:2,subtotal:36}," +
                    "{barcode:'goods_02',name:'商品02',price:18,num:2,subtotal:36}]}";
            mockMvc.perform(post("/instock/save")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 采购入库单修改
     */
    @Test
    public void testUpdate(){
        try {
            String json = "{id:'a10426cf21f140e1af7a613e65cea25d',incode:'tom-update','indate':'2018-02-11',remark:'update remark',line: ["
                    + "{id:'d298dfeb9d4e453099b940509623bf28',barcode:'goods_05',name:'商品05-update',price:18,num:2,subtotal:36}]}";
            mockMvc.perform(post("/instock/save")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
