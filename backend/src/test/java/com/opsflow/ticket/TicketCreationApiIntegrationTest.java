package com.opsflow.ticket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketCreationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requesterCanCreateTicketThroughApi() throws Exception {
        String requestBody = """
                {
                  "title": "Integration test ticket",
                  "description": "Created by the ticket creation API integration test.",
                  "affectedSystem": "OpsFlow",
                  "impact": "MEDIUM",
                  "urgency": "HIGH"
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("requester@opsflow.demo", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber", matchesPattern("OPS-\\d{4}-\\d{4,}")))
                .andExpect(jsonPath("$.title").value("Integration test ticket"))
                .andExpect(jsonPath("$.affectedSystem").value("OpsFlow"))
                .andExpect(jsonPath("$.impact").value("MEDIUM"))
                .andExpect(jsonPath("$.urgency").value("HIGH"))
                .andExpect(jsonPath("$.priority").value("P2"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.requesterEmail").value("requester@opsflow.demo"))
                .andExpect(jsonPath("$.slaRisk").exists())
                .andExpect(jsonPath("$.slaDueAt").exists());
    }

    @Test
    void analystCannotCreateTicketThroughApi() throws Exception {
        String requestBody = """
                {
                  "title": "Unauthorized creation test",
                  "description": "Analysts should not create requester tickets.",
                  "affectedSystem": "OpsFlow",
                  "impact": "LOW",
                  "urgency": "LOW"
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("analyst@opsflow.demo", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotCreateTicketThroughApi() throws Exception {
        String requestBody = """
                {
                  "title": "Unauthenticated creation test",
                  "description": "Unauthenticated users should not create tickets.",
                  "affectedSystem": "OpsFlow",
                  "impact": "LOW",
                  "urgency": "LOW"
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidCreateTicketRequestReturnsBadRequest() throws Exception {
        String requestBody = """
                {
                  "title": "",
                  "description": "",
                  "affectedSystem": "",
                  "impact": null,
                  "urgency": null
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("requester@opsflow.demo", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
