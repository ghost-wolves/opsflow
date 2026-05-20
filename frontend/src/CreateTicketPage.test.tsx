import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

describe('Create ticket page', () => {
  beforeEach(() => {
    localStorage.clear();

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

    window.history.pushState({}, '', '/tickets/new');
  });

  it('shows the ticket creation form for requesters', () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>,
    );

    expect(screen.getByRole('heading', { name: /Create Ticket/i })).toBeInTheDocument();
    expect(screen.getByText(/Submit a new incident for triage and SLA tracking/i)).toBeInTheDocument();

    expect(screen.getByLabelText(/Title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Description/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Affected System/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Impact/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Urgency/i)).toBeInTheDocument();

    expect(screen.getByRole('button', { name: /Get Triage Suggestion/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Create Ticket/i })).toBeInTheDocument();
  });

  it('blocks non-requesters from creating tickets', () => {
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
    expect(screen.getByText(/Only requesters can create tickets/i)).toBeInTheDocument();
  });
});
