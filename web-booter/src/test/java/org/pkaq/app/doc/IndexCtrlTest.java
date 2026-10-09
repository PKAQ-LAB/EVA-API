package org.pkaq.app.doc;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 应用首页路由的隔离验证，不启动数据库或完整应用。
 *
 * @author PKAQ
 * @date 2026-10-09
 */
class IndexCtrlTest {
    /** 默认入口继续返回原有文档相对地址。 */
    @Test
    void redirectsRootToDocumentation() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new IndexCtrl()).build();

        mvc.perform(get("/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("doc.html"));
    }

    /** 应用设置上下文路径时仍使用相对文档地址。 */
    @Test
    void keepsRelativeRedirectUnderContextPath() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new IndexCtrl()).build();

        mvc.perform(get("/api/").contextPath("/api"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("doc.html"));
    }
}
