import { expect, test } from '@playwright/test';

test('requester can log in, create a ticket, and view it', async ({ page }) => {
  const uniqueId = Date.now();
  const ticketTitle = `E2E requester ticket ${uniqueId}`;

  await page.goto('/login');

  await page.getByRole('button', { name: /Login as Requester/i }).click();

  await expect(page.getByText(/Signed in as Riley Requester/i)).toBeVisible();

  await page.goto('/tickets/new');

  await expect(page.getByRole('heading', { name: /Create Ticket/i })).toBeVisible();

  await page.getByLabel(/Title/i).fill(ticketTitle);
  await page
    .getByLabel(/Description/i)
    .fill('Created by the requester Playwright E2E workflow test.');
  await page.getByLabel(/Affected System/i).fill('Identity Portal');
  await page.getByLabel(/Impact/i).selectOption('MEDIUM');
  await page.getByLabel(/Urgency/i).selectOption('HIGH');

  await page.getByRole('button', { name: /^Create Ticket$/i }).click();

  await expect(page.getByText(/created successfully/i)).toBeVisible();
  await expect(page.getByText(/Priority: P2/i)).toBeVisible();
  await expect(page.getByText(/Status: NEW/i)).toBeVisible();

  await page.goto('/tickets');

  await expect(page.getByRole('heading', { name: /My Tickets/i })).toBeVisible();
  await expect(page.getByText(ticketTitle)).toBeVisible();

  await page.getByRole('link', { name: ticketTitle }).click();

  await expect(page.getByRole('heading', { name: /Ticket Detail/i })).toBeVisible();
  await expect(page.getByText(ticketTitle)).toBeVisible();
  await expect(page.getByText(/Identity Portal/i)).toBeVisible();
  await expect(page.getByText(/P2/i)).toBeVisible();
});
