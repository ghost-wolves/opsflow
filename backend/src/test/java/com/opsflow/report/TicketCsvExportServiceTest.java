package com.opsflow.report;

import com.opsflow.ticket.*;
import com.opsflow.user.AppUser;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TicketCsvExportServiceTest {

    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final SlaRiskCalculationService slaRiskCalculationService = mock(SlaRiskCalculationService.class);

    private final TicketCsvExportService service = new TicketCsvExportService(
            ticketRepository,
            slaRiskCalculationService
    );

    @Test
    void exportsTicketsAsCsvWithHeaderAndRows() {
        AppUser requester = mock(AppUser.class);
        AppUser analyst = mock(AppUser.class);

        when(requester.getEmail()).thenReturn("requester@opsflow.demo");
        when(requester.getDisplayName()).thenReturn("Riley Requester");
        when(analyst.getEmail()).thenReturn("analyst@opsflow.demo");
        when(analyst.getDisplayName()).thenReturn("Alex Analyst");

        Ticket ticket = new Ticket(
                "OPS-2026-9999",
                "CSV export, quoted title",
                "Description with, comma",
                "OpsFlow",
                Impact.MEDIUM,
                Urgency.HIGH,
                Priority.P2,
                TicketStatus.ASSIGNED,
                requester,
                OffsetDateTime.parse("2026-05-20T12:00:00Z")
        );

        ticket.assignTo(analyst);

        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(ticket));
        when(slaRiskCalculationService.calculate(ticket)).thenReturn(SlaRisk.ON_TRACK);

        String csv = service.exportTicketsCsv();

        assertTrue(csv.contains("Ticket Number,Title,Description,Affected System"));
        assertTrue(csv.contains("OPS-2026-9999"));
        assertTrue(csv.contains("\"CSV export, quoted title\""));
        assertTrue(csv.contains("\"Description with, comma\""));
        assertTrue(csv.contains("requester@opsflow.demo"));
        assertTrue(csv.contains("analyst@opsflow.demo"));
        assertTrue(csv.contains("ON_TRACK"));
    }

    @Test
    void exportsHeaderWhenNoTicketsExist() {
        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        String csv = service.exportTicketsCsv();

        assertTrue(csv.startsWith("Ticket Number,Title,Description,Affected System"));
    }
}
