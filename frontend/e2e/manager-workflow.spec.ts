import { expect, test } from '@playwright/test';

test('manager can view dashboard, all tickets, ticket detail, and download CSV report', async ({ page, request }) => {
  const uniqueId = Date.now();
  const ticketTitle = `E2E manager ticket ${uniqueId}`;

  const requesterAuth = Buffer.from('requester@opsflow.demo:password123').toString('base64');

  const createResponse = await request.post('http://localhost:8080/api/tickets', {
    headers: {
      Authorization: `Basic ${requesterAuth}`,
      'Content-Type': 'application/json',
    },
    data: {
      title: ticketTitle,
      description: 'Created so the manager E2E test can review dashboard, all tickets, and reports.',
      affectedSystem: 'OpsFlow',
      impact: 'MEDIUM',
      urgency: 'HIGH',
    },
  });

  expect(createResponse.ok()).toBeTruthy();

  await page.goto('/');

  await page.evaluate(() => {
    localStorage.setItem(
      'opsflow.user',
      JSON.stringify({
        userId: 3,
        email: 'manager@opsflow.demo',
        displayName: 'Morgan Manager',
        roles: ['MANAGER'],
        message: 'Login successful',
      }),
    );

    localStorage.setItem(
      'opsflow.credentials',
      btoa('manager@opsflow.demo:password123'),
    );
  });

  await page.goto('/dashboard');

  await expect(page.getByRole('heading', { name: /Manager Dashboard/i })).toBeVisible();
  await expect(page.getByText(/Total Tickets/i)).toBeVisible();
  await expect(page.getByText(/Unassigned/i)).toBeVisible();
  await expect(page.getByText(/Overdue/i).first()).toBeVisible();
  await expect(page.getByText(/Breached SLA/i)).toBeVisible();
  await expect(page.getByRole('heading', { name: /Status Breakdown/i })).toBeVisible();
  await expect(page.getByRole('heading', { name: /SLA Risk/i })).toBeVisible();

  await page.goto('/all-tickets');

  await expect(page.getByRole('heading', { name: /All Tickets/i })).toBeVisible();
  await expect(page.getByText(ticketTitle)).toBeVisible();
  await expect(page.getByText(/Riley Requester/i).first()).toBeVisible();
  await expect(page.getByText(/P2/i).first()).toBeVisible();

  await page.getByRole('link', { name: ticketTitle }).click();

  await expect(page.getByRole('heading', { name: /Ticket Detail/i })).toBeVisible();
  await expect(page.getByText(ticketTitle)).toBeVisible();
  await expect(page.getByText(/OpsFlow/i).first()).toBeVisible();
  await expect(page.getByText(/Audit Trail/i)).toBeVisible();

  await page.goto('/reports');

  await expect(page.getByRole('heading', { name: /Reports/i })).toBeVisible();
  await expect(page.getByText(/Ticket CSV Export/i)).toBeVisible();

  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('button', { name: /Download Ticket CSV/i }).click();
  const download = await downloadPromise;

  expect(download.suggestedFilename()).toBe('opsflow-tickets.csv');
});
