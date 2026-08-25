package org.pkaq.web.core.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eva.cloud.enable=false")
@AutoConfigureMockMvc
@Import(RequestFilter.class)
class RequestFilterDisabledIntegrationTest {
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
    void allowsRequestWithoutGatewayHeaderWhenCloudModeIsDisabled() throws Exception {
        mockMvc.perform(get("/auth/fetchDicts"))
                .andExpect(status().isOk());
    }
}