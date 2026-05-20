# OpsFlow Reporting

OpsFlow includes manager-only reporting for exporting ticket data.

## Ticket CSV Export

Managers can export all tickets as a CSV file from the Reports page.

Frontend path:

```text
/reports
```

Backend endpoint:

```http
GET /api/reports/tickets.csv
```

Required role:

```text
MANAGER
```

Non-manager users receive HTTP 403 Forbidden.

## Exported Fields

The ticket CSV includes the following columns:

```text
Ticket Number
Title
Description
Affected System
Impact
Urgency
Priority
Status
Requester Email
Requester Name
Assigned To Email
Assigned To Name
SLA Risk
SLA Due At
SLA Breached
Created At
Updated At
Resolved At
Closed At
```

## Example Usage

Using curl:

```bash
curl -u manager@opsflow.demo:password123 \
  http://localhost:8080/api/reports/tickets.csv \
  -o opsflow-tickets.csv
```

The downloaded file can be opened in spreadsheet tools for ticket review, SLA reporting, and operational analysis.

## Security Notes

The export endpoint is restricted to users with the MANAGER role.

The frontend Reports page also hides export functionality from non-manager users and displays an access-denied message if a non-manager visits the page directly.
