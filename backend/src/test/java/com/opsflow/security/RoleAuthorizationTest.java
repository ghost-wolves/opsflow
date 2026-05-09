package com.opsflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requesterCannotAccessManagerOnlyEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/access-check")
                        .with(httpBasic("requester@opsflow.demo", "password123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystCannotAccessManagerOnlyEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/access-check")
                        .with(httpBasic("analyst@opsflow.demo", "password123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanAccessManagerOnlyEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/access-check")
                        .with(httpBasic("manager@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("AUTHORIZED")))
                .andExpect(jsonPath("$.scope", is("MANAGER")));
    }
}
