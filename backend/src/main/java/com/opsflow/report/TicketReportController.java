package com.opsflow.report;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class TicketReportController {

    private final TicketCsvExportService ticketCsvExportService;

    public TicketReportController(TicketCsvExportService ticketCsvExportService) {
        this.ticketCsvExportService = ticketCsvExportService;
    }

    @GetMapping(value = "/tickets.csv", produces = "text/csv")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> exportTicketsCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"opsflow-tickets.csv\"")
                .contentType(new MediaType("text", "csv"))
                .body(ticketCsvExportService.exportTicketsCsv());
    }
}
