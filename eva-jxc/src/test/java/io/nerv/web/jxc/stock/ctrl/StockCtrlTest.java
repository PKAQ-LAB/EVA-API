package io.nerv.web.jxc.stock.ctrl;

import org.junit.Test;
import io.nerv.web.BaseTest;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/10/16 9:35
 */
public class StockCtrlTest extends BaseTest {

    @Test
    public void testt() {
        try {
            mockMvc.perform(post("/stock/ttt"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}