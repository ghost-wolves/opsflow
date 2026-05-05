import { render, screen } from '@testing-library/react';
import App from './App';

describe('App', () => {
  it('renders the OpsFlow landing page', () => {
    render(<App />);

    expect(screen.getByRole('heading', { name: /OpsFlow/i })).toBeInTheDocument();
    expect(screen.getByText(/Incident Ticket Triage and SLA Management/i)).toBeInTheDocument();
  });
});
