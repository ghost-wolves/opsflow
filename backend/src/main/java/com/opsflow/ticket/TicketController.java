package com.opsflow.ticket;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    @PreAuthorize("hasAnyRole('REQUESTER', 'ANALYST', 'MANAGER')")
    public List<TicketResponse> listTickets(Authentication authentication) {
        return ticketService.listTicketsForUser(authentication.getName());
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('REQUESTER', 'ANALYST', 'MANAGER')")
    public TicketResponse getTicket(
            @PathVariable Long ticketId,
            Authentication authentication
    ) {
        return ticketService.getTicketForUser(ticketId, authentication.getName());
    }

    @PatchMapping("/{ticketId}/status")
    @PreAuthorize("hasAnyRole('ANALYST', 'MANAGER')")
    public TicketResponse updateStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            Authentication authentication
    ) {
        return ticketService.updateTicketStatus(ticketId, request, authentication.getName());
    }
}
