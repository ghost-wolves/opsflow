package com.opsflow.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommentsAndAuditApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void requesterAndAnalystCanAddAndViewAllowedComments() throws Exception {
        long ticketId = createRequesterTicket("Comments integration test");

        addPublicRequesterComment(ticketId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId))
                .andExpect(jsonPath("$.authorEmail").value("requester@opsflow.demo"))
                .andExpect(jsonPath("$.internal").value(false));

        addInternalAnalystComment(ticketId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId))
                .andExpect(jsonPath("$.authorEmail").value("analyst@opsflow.demo"))
                .andExpect(jsonPath("$.internal").value(true));

        String requesterCommentsJson = mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId)
                        .with(httpBasic("requester@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode requesterComments = objectMapper.readTree(requesterCommentsJson);
        assertEquals(1, requesterComments.size());
        assertFalse(requesterComments.get(0).get("internal").asBoolean());
        assertEquals("Public requester comment from integration test.", requesterComments.get(0).get("body").asText());

        String analystCommentsJson = mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId)
                        .with(httpBasic("analyst@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode analystComments = objectMapper.readTree(analystCommentsJson);
        assertEquals(2, analystComments.size());
        assertTrue(hasCommentBody(analystComments, "Public requester comment from integration test."));
        assertTrue(hasCommentBody(analystComments, "Internal analyst comment from integration test."));
        assertTrue(hasInternalComment(analystComments));
    }

    @Test
    void requesterCannotCreateInternalComment() throws Exception {
        long ticketId = createRequesterTicket("Requester internal comment forbidden test");

        mockMvc.perform(post("/api/tickets/{ticketId}/comments", ticketId)
                        .with(httpBasic("requester@opsflow.demo", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "body": "Requester should not be able to create an internal comment.",
                                  "internal": true
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAccessCommentsOrAuditEvents() throws Exception {
        long ticketId = createRequesterTicket("Unauthenticated comments audit forbidden test");

        mockMvc.perform(get("/api/tickets/{ticketId}/comments", ticketId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tickets/{ticketId}/comments", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "body": "Unauthenticated comment should fail.",
                                  "internal": false
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/tickets/{ticketId}/audit-events", ticketId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void auditTrailIncludesTicketCreatedAndCommentAddedEvents() throws Exception {
        long ticketId = createRequesterTicket("Audit event integration test");

        addPublicRequesterComment(ticketId)
                .andExpect(status().isOk());

        String auditJson = mockMvc.perform(get("/api/tickets/{ticketId}/audit-events", ticketId)
                        .with(httpBasic("requester@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode auditEvents = objectMapper.readTree(auditJson);
        List<String> eventTypes = new ArrayList<>();

        for (JsonNode event : auditEvents) {
            eventTypes.add(event.get("eventType").asText());
        }

        assertTrue(eventTypes.contains("TICKET_CREATED"));
        assertTrue(eventTypes.contains("COMMENT_ADDED"));
    }

    private long createRequesterTicket(String title) throws Exception {
        String requestBody = """
                {
                  "title": "%s",
                  "description": "Created by comments and audit integration tests.",
                  "affectedSystem": "OpsFlow",
                  "impact": "LOW",
                  "urgency": "LOW"
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

    private org.springframework.test.web.servlet.ResultActions addPublicRequesterComment(long ticketId) throws Exception {
        return mockMvc.perform(post("/api/tickets/{ticketId}/comments", ticketId)
                .with(httpBasic("requester@opsflow.demo", "password123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "body": "Public requester comment from integration test.",
                          "internal": false
                        }
                        """));
    }

    private org.springframework.test.web.servlet.ResultActions addInternalAnalystComment(long ticketId) throws Exception {
        return mockMvc.perform(post("/api/tickets/{ticketId}/comments", ticketId)
                .with(httpBasic("analyst@opsflow.demo", "password123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "body": "Internal analyst comment from integration test.",
                          "internal": true
                        }
                        """));
    }

    private boolean hasCommentBody(JsonNode comments, String body) {
        for (JsonNode comment : comments) {
            if (body.equals(comment.get("body").asText())) {
                return true;
            }
        }

        return false;
    }

    private boolean hasInternalComment(JsonNode comments) {
        for (JsonNode comment : comments) {
            if (comment.get("internal").asBoolean()) {
                return true;
            }
        }

        return false;
    }
}
