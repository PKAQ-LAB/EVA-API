package org.pkaq.web.core.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pkaq.core.constant.CommonConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eva.cloud.enable=true")
@AutoConfigureMockMvc
@Import(RequestFilter.class)
class RequestFilterEnabledIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext applicationContext;
    @Autowired
    private RequestFilter requestFilter;

    @BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .addFilters(requestFilter)
                .build();
    }

    @Test
    void rejectsMissingGatewayHeaderWithoutErrorDispatch() throws Exception {
        mockMvc.perform(get("/auth/fetchDicts"))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertNull(result.getResponse().getForwardedUrl()));
    }

    @Test
    void rejectsWrongGatewayHeader() throws Exception {
        mockMvc.perform(get("/auth/fetchDicts")
                        .header(CommonConstant.X_GATEWAY_HEADER, "wrong"))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsCorrectGatewayHeader() throws Exception {
        mockMvc.perform(get("/auth/fetchDicts")
                        .header(CommonConstant.X_GATEWAY_HEADER, CommonConstant.X_GATEWAY_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    void allowsCorsPreflightWithoutGatewayHeader() throws Exception {
        mockMvc.perform(options("/auth/fetchDicts")
                        .header(HttpHeaders.ORIGIN, "https://example.test")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk());
    }
}