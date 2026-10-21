package com.hostelhub.integration;

import com.hostelhub.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void applicationContextStarts() {
        // Context initialization itself is the assertion.
    }

    @Test
    void csrfEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk());
    }

    @Test
    void currentUserEndpointRejectsAnonymousRequest()
            throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invoicesEndpointRejectsAnonymousRequest()
            throws Exception {
        mockMvc.perform(get("/api/billing/invoices/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void paymentsEndpointRejectsAnonymousRequest()
            throws Exception {
        mockMvc.perform(get("/api/payments/mine"))
                .andExpect(status().isUnauthorized());
    }
}
