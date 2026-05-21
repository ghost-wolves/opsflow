import { expect, test } from '@playwright/test';

test('analyst can claim a requester ticket, update status, and add an internal comment', async ({ page, request }) => {
  const uniqueId = Date.now();
  const ticketTitle = `E2E analyst ticket ${uniqueId}`;

  const requesterAuth = Buffer.from('requester@opsflow.demo:password123').toString('base64');

  const createResponse = await request.post('http://localhost:8080/api/tickets', {
    headers: {
      Authorization: `Basic ${requesterAuth}`,
      'Content-Type': 'application/json',
    },
    data: {
      title: ticketTitle,
      description: 'Created so the analyst E2E test can claim and work this ticket.',
      affectedSystem: 'OpsFlow',
      impact: 'LOW',
      urgency: 'LOW',
    },
  });

  expect(createResponse.ok()).toBeTruthy();

  await page.goto('/');

  await page.evaluate(() => {
    localStorage.setItem(
      'opsflow.user',
      JSON.stringify({
        userId: 2,
        email: 'analyst@opsflow.demo',
        displayName: 'Alex Analyst',
        roles: ['ANALYST'],
        message: 'Login successful',
      }),
    );

    localStorage.setItem(
      'opsflow.credentials',
      btoa('analyst@opsflow.demo:password123'),
    );
  });

  await page.goto('/queue');

  await expect(page.getByRole('heading', { name: /Analyst Queue/i })).toBeVisible();
  await expect(page.getByText(ticketTitle)).toBeVisible();

  const ticketRow = page.locator('tr').filter({ hasText: ticketTitle });
  await ticketRow.getByRole('button', { name: /Claim/i }).click();

  await expect(page.getByText(/Claimed OPS-/i)).toBeVisible();

  await page.getByRole('link', { name: ticketTitle }).click();

  await expect(page.getByRole('heading', { name: /Ticket Detail/i })).toBeVisible();
  await expect(page.getByText(ticketTitle)).toBeVisible();
  await expect(page.getByText('Alex Analyst', { exact: true })).toBeVisible();
  await expect(page.getByLabel(/New Status/i)).toHaveValue('ASSIGNED');

  await page.getByLabel(/New Status/i).selectOption('IN_PROGRESS');
  await page.getByRole('button', { name: /Update Status/i }).click();

  await expect(page.getByText(/Status updated to IN_PROGRESS/i)).toBeVisible();
  await expect(page.getByLabel(/New Status/i)).toHaveValue('IN_PROGRESS');

  await page
    .getByLabel(/Add Comment/i)
    .fill('Internal analyst note from Playwright E2E test.');

  await page.getByLabel(/Internal comment/i).check();
  await page.getByRole('button', { name: /Add Comment/i }).click();

  await expect(page.getByText(/Comment added/i)).toBeVisible();
  await expect(page.getByText(/Internal analyst note from Playwright E2E test/i)).toBeVisible();
  await expect(page.getByText('Internal', { exact: true }).last()).toBeVisible();
});
