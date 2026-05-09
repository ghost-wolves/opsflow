package com.opsflow.ticket;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @PreAuthorize("hasRole('REQUESTER')")
    public TicketResponse createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication
    ) {
        return ticketService.createTicket(request, authentication.getName());
    }
}
