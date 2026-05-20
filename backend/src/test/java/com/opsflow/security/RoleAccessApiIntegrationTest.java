package com.opsflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoleAccessApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedUserCanAccessPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("opsflow-api"));
    }

    @Test
    void unauthenticatedUserCannotAccessTicketList() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requesterCanAccessTicketListButCannotAccessManagerDashboardOrReports() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .with(httpBasic("requester@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/dashboard/manager")
                        .with(httpBasic("requester@opsflow.demo", "password123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/tickets.csv")
                        .with(httpBasic("requester@opsflow.demo", "password123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystCanAccessTicketListButCannotAccessManagerDashboardOrReports() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .with(httpBasic("analyst@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/dashboard/manager")
                        .with(httpBasic("analyst@opsflow.demo", "password123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/tickets.csv")
                        .with(httpBasic("analyst@opsflow.demo", "password123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanAccessTicketListDashboardReportsAndManagerOnlyEndpoint() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .with(httpBasic("manager@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/dashboard/manager")
                        .with(httpBasic("manager@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTickets").exists())
                .andExpect(jsonPath("$.overdueTickets").exists())
                .andExpect(jsonPath("$.breachedSlaTickets").exists());

        mockMvc.perform(get("/api/reports/tickets.csv")
                        .with(httpBasic("manager@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("opsflow-tickets.csv")))
                .andExpect(content().string(containsString("Ticket Number,Title,Description,Affected System")));

        mockMvc.perform(get("/api/admin/access-check")
                        .with(httpBasic("manager@opsflow.demo", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.scope").value("MANAGER"));
    }
}
