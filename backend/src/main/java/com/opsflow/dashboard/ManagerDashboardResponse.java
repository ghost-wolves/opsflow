package com.opsflow.dashboard;

public record ManagerDashboardResponse(
        long totalTickets,
        long newTickets,
        long triagedTickets,
        long assignedTickets,
        long inProgressTickets,
        long waitingOnUserTickets,
        long resolvedTickets,
        long closedTickets,
        long reopenedTickets,
        long unassignedTickets,
        long overdueTickets,
        long dueSoonTickets,
        long breachedSlaTickets
) {
}
