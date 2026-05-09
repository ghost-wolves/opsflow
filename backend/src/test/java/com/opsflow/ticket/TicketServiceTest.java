package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import com.opsflow.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final TicketNumberGenerator ticketNumberGenerator = mock(TicketNumberGenerator.class);
    private final PriorityCalculationService priorityCalculationService = mock(PriorityCalculationService.class);
    private final SlaCalculationService slaCalculationService = mock(SlaCalculationService.class);

    private final TicketService ticketService = new TicketService(
            ticketRepository,
            appUserRepository,
            ticketNumberGenerator,
            priorityCalculationService,
            slaCalculationService
    );

    @Test
    void createTicketGeneratesNumberCalculatesPriorityCalculatesSlaAndSavesNewTicket() {
        AppUser requester = mock(AppUser.class);
        OffsetDateTime slaDueAt = OffsetDateTime.parse("2026-05-09T16:00:00Z");

        CreateTicketRequest request = new CreateTicketRequest(
                "Billing export failed",
                "Customers cannot download invoices because the billing export failed.",
                "Billing Portal",
                Impact.HIGH,
                Urgency.HIGH
        );

        when(requester.getId()).thenReturn(1L);
        when(requester.getEmail()).thenReturn("requester@opsflow.demo");
        when(requester.getDisplayName()).thenReturn("Riley Requester");
        when(requester.isEnabled()).thenReturn(true);

        when(appUserRepository.findByEmailIgnoreCase("requester@opsflow.demo"))
                .thenReturn(Optional.of(requester));
        when(ticketRepository.count()).thenReturn(0L);
        when(ticketNumberGenerator.generate(1L)).thenReturn("OPS-2026-0001");
        when(priorityCalculationService.calculate(Impact.HIGH, Urgency.HIGH)).thenReturn(Priority.P1);
        when(slaCalculationService.calculateDueAt(Priority.P1)).thenReturn(slaDueAt);

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.createTicket(request, "requester@opsflow.demo");

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());

        Ticket savedTicket = ticketCaptor.getValue();

        assertEquals("OPS-2026-0001", savedTicket.getTicketNumber());
        assertEquals("Billing export failed", savedTicket.getTitle());
        assertEquals("Customers cannot download invoices because the billing export failed.", savedTicket.getDescription());
        assertEquals("Billing Portal", savedTicket.getAffectedSystem());
        assertEquals(Impact.HIGH, savedTicket.getImpact());
        assertEquals(Urgency.HIGH, savedTicket.getUrgency());
        assertEquals(Priority.P1, savedTicket.getPriority());
        assertEquals(TicketStatus.NEW, savedTicket.getStatus());
        assertEquals(requester, savedTicket.getRequester());
        assertEquals(slaDueAt, savedTicket.getSlaDueAt());

        assertEquals("OPS-2026-0001", response.ticketNumber());
        assertEquals(Priority.P1, response.priority());
        assertEquals(TicketStatus.NEW, response.status());
        assertEquals("requester@opsflow.demo", response.requesterEmail());

        verify(ticketRepository).count();
        verify(ticketNumberGenerator).generate(1L);
        verify(priorityCalculationService).calculate(Impact.HIGH, Urgency.HIGH);
        verify(slaCalculationService).calculateDueAt(Priority.P1);
    }

    @Test
    void createTicketRejectsUnknownRequester() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Billing export failed",
                "Customers cannot download invoices because the billing export failed.",
                "Billing Portal",
                Impact.HIGH,
                Urgency.HIGH
        );

        when(appUserRepository.findByEmailIgnoreCase("missing@opsflow.demo"))
                .thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> ticketService.createTicket(request, "missing@opsflow.demo")
        );

        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}
