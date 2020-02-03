package io.nerv.web.sys.user.ctrl;

import com.alibaba.fastjson.JSON;
import io.nerv.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Slf4j
public class AuthCtrlTest extends BaseTest {
    @Test
    public void testLogin(){
        try {
            String json = "{account: 'admin', password: '0192023a7bbd73250516f069df18b500'}";
            MvcResult re = this.mockMvc.perform(post("/auth/login")
                    .content(json)).andReturn();

            System.out.println("------------------------>");
            System.out.println(JSON.toJSONString(re.getResponse()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}