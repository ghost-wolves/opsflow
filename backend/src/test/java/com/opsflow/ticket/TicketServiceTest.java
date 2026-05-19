package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import com.opsflow.user.AppUserRepository;
import com.opsflow.user.Role;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final TicketNumberGenerator ticketNumberGenerator = mock(TicketNumberGenerator.class);
    private final PriorityCalculationService priorityCalculationService = mock(PriorityCalculationService.class);
    private final SlaCalculationService slaCalculationService = mock(SlaCalculationService.class);
    private final TicketStatusTransitionService ticketStatusTransitionService = mock(TicketStatusTransitionService.class);
    private final TicketAuditService ticketAuditService = mock(TicketAuditService.class);

    private final TicketService ticketService = new TicketService(
            ticketRepository,
            appUserRepository,
            ticketNumberGenerator,
            priorityCalculationService,
            slaCalculationService,
            ticketStatusTransitionService,
            ticketAuditService
    );

    @Test
    void createTicketGeneratesNumberCalculatesPriorityCalculatesSlaAndSavesNewTicket() {
        AppUser requester = mockUser(1L, "requester@opsflow.demo", "Riley Requester", "REQUESTER");
        OffsetDateTime slaDueAt = OffsetDateTime.parse("2026-05-09T16:00:00Z");

        CreateTicketRequest request = new CreateTicketRequest(
                "Billing export failed",
                "Customers cannot download invoices because the billing export failed.",
                "Billing Portal",
                Impact.HIGH,
                Urgency.HIGH
        );

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
        verify(ticketAuditService).record(
                any(Ticket.class),
                eq(requester),
                eq(TicketAuditEventType.TICKET_CREATED),
                isNull(),
                eq(TicketStatus.NEW.name()),
                contains("was created")
        );
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

    @Test
    void requesterSeesOnlyOwnTickets() {
        AppUser requester = mockUser(1L, "requester@opsflow.demo", "Riley Requester", "REQUESTER");

        when(appUserRepository.findByEmailIgnoreCase("requester@opsflow.demo"))
                .thenReturn(Optional.of(requester));

        ticketService.listTicketsForUser("requester@opsflow.demo");

        verify(ticketRepository).findByRequesterEmailIgnoreCaseOrderByCreatedAtDesc("requester@opsflow.demo");
        verify(ticketRepository, never()).findVisibleToAnalyst(anyString());
        verify(ticketRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void analystSeesAssignedAndUnassignedTickets() {
        AppUser analyst = mockUser(2L, "analyst@opsflow.demo", "Alex Analyst", "ANALYST");

        when(appUserRepository.findByEmailIgnoreCase("analyst@opsflow.demo"))
                .thenReturn(Optional.of(analyst));

        ticketService.listTicketsForUser("analyst@opsflow.demo");

        verify(ticketRepository).findVisibleToAnalyst("analyst@opsflow.demo");
        verify(ticketRepository, never()).findByRequesterEmailIgnoreCaseOrderByCreatedAtDesc(anyString());
        verify(ticketRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void managerSeesAllTickets() {
        AppUser manager = mockUser(3L, "manager@opsflow.demo", "Morgan Manager", "MANAGER");

        when(appUserRepository.findByEmailIgnoreCase("manager@opsflow.demo"))
                .thenReturn(Optional.of(manager));

        ticketService.listTicketsForUser("manager@opsflow.demo");

        verify(ticketRepository).findAllByOrderByCreatedAtDesc();
        verify(ticketRepository, never()).findByRequesterEmailIgnoreCaseOrderByCreatedAtDesc(anyString());
        verify(ticketRepository, never()).findVisibleToAnalyst(anyString());
    }


    @Test
    void updateTicketStatusCreatesAuditEvent() {
        AppUser analyst = mockUser(2L, "analyst@opsflow.demo", "Alex Analyst", "ANALYST");
        AppUser requester = mockUser(1L, "requester@opsflow.demo", "Riley Requester", "REQUESTER");

        Ticket ticket = new Ticket(
                "OPS-2026-1234",
                "Test ticket",
                "Test description",
                "Test System",
                Impact.MEDIUM,
                Urgency.MEDIUM,
                Priority.P3,
                TicketStatus.NEW,
                requester,
                OffsetDateTime.parse("2026-05-20T12:00:00Z")
        );

        when(appUserRepository.findByEmailIgnoreCase("analyst@opsflow.demo"))
                .thenReturn(Optional.of(analyst));
        when(ticketRepository.findById(1234L))
                .thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.updateTicketStatus(
                1234L,
                new UpdateTicketStatusRequest(TicketStatus.TRIAGED),
                "analyst@opsflow.demo"
        );

        assertEquals(TicketStatus.TRIAGED, response.status());

        verify(ticketStatusTransitionService).validateTransition(TicketStatus.NEW, TicketStatus.TRIAGED);
        verify(ticketAuditService).record(
                any(Ticket.class),
                eq(analyst),
                eq(TicketAuditEventType.STATUS_CHANGED),
                eq(TicketStatus.NEW.name()),
                eq(TicketStatus.TRIAGED.name()),
                contains("Status changed")
        );
    }

    private AppUser mockUser(Long id, String email, String displayName, String roleName) {
        AppUser user = mock(AppUser.class);
        Role role = mock(Role.class);

        when(role.getName()).thenReturn(roleName);
        when(user.getId()).thenReturn(id);
        when(user.getEmail()).thenReturn(email);
        when(user.getDisplayName()).thenReturn(displayName);
        when(user.isEnabled()).thenReturn(true);
        when(user.getRoles()).thenReturn(Set.of(role));

        return user;
    }
}
