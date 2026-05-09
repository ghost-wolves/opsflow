package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import com.opsflow.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void canSaveAndRetrieveTicketByTicketNumber() {
        AppUser requester = appUserRepository.findByEmailIgnoreCase("requester@opsflow.demo")
                .orElseThrow();

        String ticketNumber = "OPS-TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Ticket ticket = new Ticket(
                ticketNumber,
                "Cannot access billing portal",
                "The billing portal returns an error when I try to log in.",
                "Billing Portal",
                Impact.HIGH,
                Urgency.HIGH,
                Priority.P1,
                TicketStatus.NEW,
                requester,
                OffsetDateTime.now().plusHours(2)
        );

        ticketRepository.saveAndFlush(ticket);

        var found = ticketRepository.findByTicketNumber(ticketNumber);

        assertTrue(found.isPresent());
        assertEquals(ticketNumber, found.get().getTicketNumber());
        assertEquals("Cannot access billing portal", found.get().getTitle());
        assertEquals(Priority.P1, found.get().getPriority());
        assertEquals(TicketStatus.NEW, found.get().getStatus());
        assertEquals(requester.getId(), found.get().getRequester().getId());
    }
}
