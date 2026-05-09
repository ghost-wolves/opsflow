import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from './App';

describe('App', () => {
  it('renders the OpsFlow landing page', () => {
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: /OpsFlow/i })).toBeInTheDocument();
    expect(screen.getByText(/Incident Ticket Triage and SLA Management/i)).toBeInTheDocument();
  });
});
