package org.pkaq.core.auth;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pkaq.core.constant.CommonConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eva.jwt.persistence=true")
@AutoConfigureMockMvc
class LogoutIntegrationTest {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.applicationContext)
                .apply(springSecurity(this.springSecurityFilterChain))
                .build();
    }

    @Test
    void anonymousLogoutIsIdempotentAndClearsCookies() throws Exception {
        this.mockMvc.perform(get("/auth/logout")
                        .cookie(new Cookie(CommonConstant.ACCESS_TOKEN_KEY, "expired-access"))
                        .cookie(new Cookie(CommonConstant.REFRESH_TOKEN_KEY, "expired-refresh"))
                        .cookie(new Cookie(CommonConstant.USER_KEY, "stale-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("0005"))
                .andExpect(jsonPath("$.message").value("已经成功退出登录"))
                .andExpect(cookie().maxAge(CommonConstant.ACCESS_TOKEN_KEY, 0))
                .andExpect(cookie().maxAge(CommonConstant.REFRESH_TOKEN_KEY, 0))
                .andExpect(cookie().maxAge(CommonConstant.USER_KEY, 0));
    }
}
