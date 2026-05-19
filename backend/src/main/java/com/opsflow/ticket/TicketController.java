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
    private final TicketCommentService ticketCommentService;
    private final TicketAuditService ticketAuditService;
    private final TriageSuggestionService triageSuggestionService;

    public TicketController(
            TicketService ticketService,
            TicketCommentService ticketCommentService,
            TicketAuditService ticketAuditService,
            TriageSuggestionService triageSuggestionService
    ) {
        this.ticketService = ticketService;
        this.ticketCommentService = ticketCommentService;
        this.ticketAuditService = ticketAuditService;
        this.triageSuggestionService = triageSuggestionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('REQUESTER')")
    public TicketResponse createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication
    ) {
        return ticketService.createTicket(request, authentication.getName());
    }

    @PostMapping("/triage-suggestion")
    @PreAuthorize("hasRole('REQUESTER')")
    public TriageSuggestionResponse suggestTriage(
            @Valid @RequestBody CreateTicketRequest request
    ) {
        return triageSuggestionService.suggest(
                request.title(),
                request.description(),
                request.affectedSystem()
        );
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

    @PatchMapping("/{ticketId}/assign")
    @PreAuthorize("hasRole('MANAGER')")
    public TicketResponse assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest request,
            Authentication authentication
    ) {
        return ticketService.assignTicket(ticketId, request, authentication.getName());
    }

    @PatchMapping("/{ticketId}/claim")
    @PreAuthorize("hasRole('ANALYST')")
    public TicketResponse claimTicket(
            @PathVariable Long ticketId,
            Authentication authentication
    ) {
        return ticketService.claimTicket(ticketId, authentication.getName());
    }

    @PostMapping("/{ticketId}/comments")
    @PreAuthorize("hasAnyRole('REQUESTER', 'ANALYST', 'MANAGER')")
    public TicketCommentResponse addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateTicketCommentRequest request,
            Authentication authentication
    ) {
        return ticketCommentService.addComment(ticketId, request, authentication.getName());
    }

    @GetMapping("/{ticketId}/comments")
    @PreAuthorize("hasAnyRole('REQUESTER', 'ANALYST', 'MANAGER')")
    public List<TicketCommentResponse> listComments(
            @PathVariable Long ticketId,
            Authentication authentication
    ) {
        return ticketCommentService.listComments(ticketId, authentication.getName());
    }

    @GetMapping("/{ticketId}/audit-events")
    @PreAuthorize("hasAnyRole('REQUESTER', 'ANALYST', 'MANAGER')")
    public List<TicketAuditEventResponse> listAuditEvents(
            @PathVariable Long ticketId,
            Authentication authentication
    ) {
        return ticketAuditService.listAuditEvents(ticketId, authentication.getName());
    }
}
