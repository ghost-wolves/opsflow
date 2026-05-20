package com.opsflow.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AssignmentAndStatusApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void managerCanAssignTicketToAnalystAndAssignmentMovesNewTicketToAssigned() throws Exception {
        long ticketId = createRequesterTicket("Assignment integration test");

        String requestBody = """
                {
                  "analystEmail": "analyst@opsflow.demo"
                }
                """;

        mockMvc.perform(patch("/api/tickets/{ticketId}/assign", ticketId)
                        .with(httpBasic("manager@opsflow.demo", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.assignedToEmail").value("analyst@opsflow.demo"))
                .andExpect(jsonPath("$.assignedToDisplayName").value("Alex Analyst"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void analystCanClaimUnassignedTicketAndClaimMovesNewTicketToAssigned() throws Exception {
        long ticketId = createRequesterTicket("Claim integration test");

        mockMvc.perform(patch("/api/tickets/{ticketId}/claim", ticketId)
                        .with(httpBasic("analyst@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.assignedToEmail").value("analyst@opsflow.demo"))
                .andExpect(jsonPath("$.assignedToDisplayName").value("Alex Analyst"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void analystCanMoveTicketThroughValidStatusWorkflow() throws Exception {
        long ticketId = createRequesterTicket("Status workflow integration test");

        patchStatus(ticketId, "TRIAGED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TRIAGED"));

        assignTicketAsManager(ticketId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        patchStatus(ticketId, "IN_PROGRESS")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        patchStatus(ticketId, "RESOLVED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedAt").exists());

        patchStatus(ticketId, "CLOSED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.closedAt").exists());
    }

    @Test
    void requesterCannotAssignClaimOrUpdateStatus() throws Exception {
        long ticketId = createRequesterTicket("Requester forbidden workflow test");

        mockMvc.perform(patch("/api/tickets/{ticketId}/assign", ticketId)
                        .with(httpBasic("requester@opsflow.demo", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "analystEmail": "analyst@opsflow.demo"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/tickets/{ticketId}/claim", ticketId)
                        .with(httpBasic("requester@opsflow.demo", "password123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/tickets/{ticketId}/status", ticketId)
                        .with(httpBasic("requester@opsflow.demo", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "TRIAGED"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAssignClaimOrUpdateStatus() throws Exception {
        long ticketId = createRequesterTicket("Unauthenticated forbidden workflow test");

        mockMvc.perform(patch("/api/tickets/{ticketId}/assign", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "analystEmail": "analyst@opsflow.demo"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/tickets/{ticketId}/claim", ticketId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/tickets/{ticketId}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "TRIAGED"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private long createRequesterTicket(String title) throws Exception {
        String requestBody = """
                {
                  "title": "%s",
                  "description": "Created by assignment and status integration tests.",
                  "affectedSystem": "OpsFlow",
                  "impact": "MEDIUM",
                  "urgency": "MEDIUM"
                }
                """.formatted(title);

        String responseBody = mockMvc.perform(post("/api/tickets")
                        .with(httpBasic("requester@opsflow.demo", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber", matchesPattern("OPS-\\d{4}-\\d{4,}")))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        return json.get("id").asLong();
    }

    private ResultActions patchStatus(long ticketId, String status) throws Exception {
        return mockMvc.perform(patch("/api/tickets/{ticketId}/status", ticketId)
                .with(httpBasic("analyst@opsflow.demo", "password123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "%s"
                        }
                        """.formatted(status)));
    }

    private ResultActions assignTicketAsManager(long ticketId) throws Exception {
        return mockMvc.perform(patch("/api/tickets/{ticketId}/assign", ticketId)
                .with(httpBasic("manager@opsflow.demo", "password123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "analystEmail": "analyst@opsflow.demo"
                        }
                        """));
    }
}
