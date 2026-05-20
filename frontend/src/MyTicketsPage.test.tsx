import { render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

const mockTickets = [
  {
    id: 101,
    ticketNumber: 'OPS-2026-0101',
    title: 'Billing portal outage',
    description: 'The billing portal is unavailable.',
    affectedSystem: 'Billing Portal',
    impact: 'HIGH',
    urgency: 'HIGH',
    priority: 'P1',
    status: 'NEW',
    requesterId: 1,
    requesterEmail: 'requester@opsflow.demo',
    requesterDisplayName: 'Riley Requester',
    assignedToId: null,
    assignedToEmail: null,
    assignedToDisplayName: null,
    slaRisk: 'OVERDUE',
    createdAt: '2026-05-20T12:00:00Z',
    updatedAt: '2026-05-20T12:00:00Z',
    slaDueAt: '2026-05-20T14:00:00Z',
    resolvedAt: null,
    closedAt: null,
    slaBreached: false,
  },
];

describe('My Tickets page', () => {
  beforeEach(() => {
    localStorage.clear();

    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockTickets,
      }),
    );

    localStorage.setItem(
      'opsflow.user',
      JSON.stringify({
        userId: 1,
        email: 'requester@opsflow.demo',
        displayName: 'Riley Requester',
        roles: ['REQUESTER'],
        message: 'Login successful',
      }),
    );

    localStorage.setItem('opsflow.credentials', btoa('requester@opsflow.demo:password123'));

    window.history.pushState({}, '', '/tickets');
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('shows requester tickets in a table', async () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>,
    );

    expect(screen.getByRole('heading', { name: /My Tickets/i })).toBeInTheDocument();
    expect(screen.getByText(/View tickets you have submitted for support/i)).toBeInTheDocument();

    expect(await screen.findByText('OPS-2026-0101')).toBeInTheDocument();
    expect(screen.getByText('Billing portal outage')).toBeInTheDocument();
    expect(screen.getByText('P1')).toBeInTheDocument();
    expect(screen.getByText('NEW')).toBeInTheDocument();
    expect(screen.getByText('OVERDUE')).toBeInTheDocument();

    expect(screen.getAllByRole('link', { name: /Create Ticket/i }).length).toBeGreaterThan(0);
    expect(fetch).toHaveBeenCalledWith('/api/tickets', {
      headers: {
        Authorization: `Basic ${btoa('requester@opsflow.demo:password123')}`,
      },
    });
  });

  it('blocks non-requesters from viewing My Tickets', () => {
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

    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>,
    );

    expect(screen.getByRole('heading', { name: /Access Denied/i })).toBeInTheDocument();
    expect(screen.getByText(/Only requesters can view My Tickets/i)).toBeInTheDocument();
  });
});
