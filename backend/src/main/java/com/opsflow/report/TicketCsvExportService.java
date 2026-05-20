package com.opsflow.report;

import com.opsflow.ticket.SlaRiskCalculationService;
import com.opsflow.ticket.Ticket;
import com.opsflow.ticket.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TicketCsvExportService {

    private final TicketRepository ticketRepository;
    private final SlaRiskCalculationService slaRiskCalculationService;

    public TicketCsvExportService(
            TicketRepository ticketRepository,
            SlaRiskCalculationService slaRiskCalculationService
    ) {
        this.ticketRepository = ticketRepository;
        this.slaRiskCalculationService = slaRiskCalculationService;
    }

    @Transactional(readOnly = true)
    public String exportTicketsCsv() {
        List<Ticket> tickets = ticketRepository.findAllByOrderByCreatedAtDesc();

        String header = csvRow(
                "Ticket Number",
                "Title",
                "Description",
                "Affected System",
                "Impact",
                "Urgency",
                "Priority",
                "Status",
                "Requester Email",
                "Requester Name",
                "Assigned To Email",
                "Assigned To Name",
                "SLA Risk",
                "SLA Due At",
                "SLA Breached",
                "Created At",
                "Updated At",
                "Resolved At",
                "Closed At"
        );

        String body = tickets.stream()
                .map(this::ticketToCsvRow)
                .collect(Collectors.joining("\n"));

        return body.isBlank() ? header + "\n" : header + "\n" + body + "\n";
    }

    private String ticketToCsvRow(Ticket ticket) {
        var requester = ticket.getRequester();
        var assignedTo = ticket.getAssignedTo();

        return csvRow(
                ticket.getTicketNumber(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getAffectedSystem(),
                ticket.getImpact().name(),
                ticket.getUrgency().name(),
                ticket.getPriority().name(),
                ticket.getStatus().name(),
                requester.getEmail(),
                requester.getDisplayName(),
                assignedTo == null ? "" : assignedTo.getEmail(),
                assignedTo == null ? "" : assignedTo.getDisplayName(),
                slaRiskCalculationService.calculate(ticket).name(),
                formatDateTime(ticket.getSlaDueAt()),
                String.valueOf(ticket.isSlaBreached()),
                formatDateTime(ticket.getCreatedAt()),
                formatDateTime(ticket.getUpdatedAt()),
                formatDateTime(ticket.getResolvedAt()),
                formatDateTime(ticket.getClosedAt())
        );
    }

    private String csvRow(String... values) {
        return Stream.of(values)
                .map(this::escapeCsv)
                .collect(Collectors.joining(","));
    }

    private String escapeCsv(String value) {
        String safeValue = value == null ? "" : value;
        String escaped = safeValue.replace("\"", "\"\"");

        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }

    private String formatDateTime(OffsetDateTime value) {
        return value == null ? "" : value.toString();
    }
}
