package org.pkaq.web.jxc.instock;

import org.junit.Test;
import org.pkaq.web.BaseTest;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 采购入库单单元测试
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 8:32
 */
public class InstockTest extends BaseTest {

    @Test
    public void testList(){
        try {
            mockMvc.perform(get("/instock/list"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 采购入库单新增
     */
    @Test
    public void testSave(){
        try {
            String json = "{incode:'RK20181018','indate':'2018-10-16',remark:'this is remarks',line: [{" +
                    "barcode:'goods_01',name:'商品01',buyprice:18,buynum:3, goodsId: 'A380'}]}";
            mockMvc.perform(post("/instock/edit")
                    .content(json))
                    .andExpect(MockMvcResultMatchers.status().isOk());
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
