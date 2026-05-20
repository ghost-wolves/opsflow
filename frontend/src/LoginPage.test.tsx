import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

describe('Login page', () => {
  beforeEach(() => {
    localStorage.clear();
    window.history.pushState({}, '', '/login');
  });

  it('shows demo login options and manual login fields', () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>,
    );

    expect(screen.getByRole('heading', { name: /Login/i })).toBeInTheDocument();

    expect(screen.getByRole('button', { name: /Login as Requester/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Login as Analyst/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Login as Manager/i })).toBeInTheDocument();

    expect(screen.getByLabelText(/Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Login$/i })).toBeInTheDocument();
  });
});
