import { render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

const mockDashboard = {
  totalTickets: 12,
  newTickets: 2,
  triagedTickets: 1,
  assignedTickets: 3,
  inProgressTickets: 2,
  waitingOnUserTickets: 1,
  resolvedTickets: 1,
  closedTickets: 1,
  reopenedTickets: 1,
  unassignedTickets: 4,
  overdueTickets: 5,
  dueSoonTickets: 2,
  breachedSlaTickets: 3,
};

describe('Manager Dashboard page', () => {
  beforeEach(() => {
    localStorage.clear();

    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => mockDashboard,
      }),
    );

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

    localStorage.setItem('opsflow.credentials', btoa('manager@opsflow.demo:password123'));

    window.history.pushState({}, '', '/dashboard');
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('shows manager dashboard metrics', async () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>,
    );

    expect(screen.getByRole('heading', { name: /Manager Dashboard/i })).toBeInTheDocument();
    expect(screen.getByText(/Monitor ticket volume, workflow status, assignment, and SLA risk/i)).toBeInTheDocument();

    expect(await screen.findByText('Total Tickets')).toBeInTheDocument();
    expect(screen.getByText('Unassigned')).toBeInTheDocument();
    expect(screen.getAllByText('Due Soon').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Overdue').length).toBeGreaterThan(0);
    expect(screen.getByText('Breached SLA')).toBeInTheDocument();

    expect(screen.getByRole('heading', { name: /Status Breakdown/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /SLA Risk/i })).toBeInTheDocument();

    expect(screen.getByText('12')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument();
    expect(screen.getAllByText('2').length).toBeGreaterThan(0);
    expect(screen.getAllByText('5').length).toBeGreaterThan(0);
    expect(screen.getAllByText('3').length).toBeGreaterThan(0);

    expect(fetch).toHaveBeenCalledWith('/api/dashboard/manager', {
      headers: {
        Authorization: `Basic ${btoa('manager@opsflow.demo:password123')}`,
      },
    });
  });

  it('blocks non-managers from viewing the dashboard', () => {
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

    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>,
    );

    expect(screen.getByRole('heading', { name: /Access Denied/i })).toBeInTheDocument();
    expect(screen.getByText(/Only managers can view the dashboard/i)).toBeInTheDocument();
  });
});
