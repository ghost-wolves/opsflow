import { FormEvent, ReactNode, useEffect, useState } from 'react';
import { Link, Navigate, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom';
import './App.css';

type LoginUser = {
  userId: number;
  email: string;
  displayName: string;
  roles: string[];
  message: string;
};

type DemoAccount = {
  label: string;
  email: string;
  password: string;
};

type TicketResponse = {
  id: number;
  ticketNumber: string;
  title: string;
  description: string;
  affectedSystem: string;
  impact: string;
  urgency: string;
  priority: string;
  status: string;
  requesterId: number;
  requesterEmail: string;
  requesterDisplayName: string;
  assignedToId: number | null;
  assignedToEmail: string | null;
  assignedToDisplayName: string | null;
  createdAt: string;
  updatedAt: string;
  slaDueAt: string;
  resolvedAt: string | null;
  closedAt: string | null;
  slaBreached: boolean;
};

const DEMO_ACCOUNTS: DemoAccount[] = [
  {
    label: 'Login as Requester',
    email: 'requester@opsflow.demo',
    password: 'password123',
  },
  {
    label: 'Login as Analyst',
    email: 'analyst@opsflow.demo',
    password: 'password123',
  },
  {
    label: 'Login as Manager',
    email: 'manager@opsflow.demo',
    password: 'password123',
  },
];

function getStoredUser(): LoginUser | null {
  const raw = localStorage.getItem('opsflow.user');

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as LoginUser;
  } catch {
    localStorage.removeItem('opsflow.user');
    localStorage.removeItem('opsflow.credentials');
    return null;
  }
}

function getAuthHeader(): Record<string, string> {
  const credentials = localStorage.getItem('opsflow.credentials');

  if (!credentials) {
    return {};
  }

  return {
    Authorization: `Basic ${credentials}`,
  };
}

function hasRole(user: LoginUser | null, role: string): boolean {
  return user?.roles.includes(role) ?? false;
}

function App() {
  const [user, setUser] = useState<LoginUser | null>(getStoredUser());

  function handleLogout() {
    localStorage.removeItem('opsflow.user');
    localStorage.removeItem('opsflow.credentials');
    setUser(null);
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <Link className="brand" to="/">
          OpsFlow
        </Link>

        <RoleAwareNavigation user={user} />

        {user ? (
          <button type="button" onClick={handleLogout}>
            Logout
          </button>
        ) : null}
      </header>

      <Routes>
        <Route path="/" element={<LandingPage user={user} />} />
        <Route path="/login" element={<LoginPage onLogin={setUser} />} />

        <Route
          path="/tickets"
          element={
            <ProtectedRoute user={user}>
              <MyTicketsPage user={user} />
            </ProtectedRoute>
          }
        />

        <Route
          path="/tickets/new"
          element={
            <ProtectedRoute user={user}>
              <CreateTicketPage user={user} />
            </ProtectedRoute>
          }
        />

        <Route
          path="/tickets/:ticketId"
          element={
            <ProtectedRoute user={user}>
              <TicketDetailPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/queue"
          element={
            <ProtectedRoute user={user}>
              <PlaceholderPage title="Queue" />
            </ProtectedRoute>
          }
        />

        <Route
          path="/assigned"
          element={
            <ProtectedRoute user={user}>
              <PlaceholderPage title="Assigned Tickets" />
            </ProtectedRoute>
          }
        />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute user={user}>
              <PlaceholderPage title="Dashboard" />
            </ProtectedRoute>
          }
        />

        <Route
          path="/reports"
          element={
            <ProtectedRoute user={user}>
              <PlaceholderPage title="Reports" />
            </ProtectedRoute>
          }
        />

        <Route
          path="/all-tickets"
          element={
            <ProtectedRoute user={user}>
              <PlaceholderPage title="All Tickets" />
            </ProtectedRoute>
          }
        />
      </Routes>
    </div>
  );
}

function RoleAwareNavigation({ user }: { user: LoginUser | null }) {
  if (!user) {
    return (
      <nav aria-label="Main navigation">
        <Link to="/login">Login</Link>
      </nav>
    );
  }

  return (
    <nav aria-label="Main navigation">
      {hasRole(user, 'REQUESTER') ? (
        <>
          <Link to="/tickets">My Tickets</Link>
          <Link to="/tickets/new">Create Ticket</Link>
        </>
      ) : null}

      {hasRole(user, 'ANALYST') ? (
        <>
          <Link to="/queue">Queue</Link>
          <Link to="/assigned">Assigned Tickets</Link>
        </>
      ) : null}

      {hasRole(user, 'MANAGER') ? (
        <>
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/reports">Reports</Link>
          <Link to="/all-tickets">All Tickets</Link>
        </>
      ) : null}
    </nav>
  );
}

function ProtectedRoute({ user, children }: { user: LoginUser | null; children: ReactNode }) {
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return children;
}

function LandingPage({ user }: { user: LoginUser | null }) {
  return (
    <main className="centered-page">
      <section className="hero-card">
        <h1>OpsFlow</h1>
        <p>Incident Ticket Triage and SLA Management</p>

        {user ? (
          <div className="login-summary">
            <strong>Signed in as {user.displayName}</strong>
            <span>{user.email}</span>
            <span>{user.roles.join(', ')}</span>
          </div>
        ) : (
          <Link className="primary-link" to="/login">
            Go to Login
          </Link>
        )}
      </section>
    </main>
  );
}

function LoginPage({ onLogin }: { onLogin: (user: LoginUser) => void }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const redirectTo =
    typeof location.state === 'object' &&
    location.state !== null &&
    'from' in location.state &&
    typeof location.state.from === 'string'
      ? location.state.from
      : '/';

  async function loginWithAccount(account: DemoAccount) {
    setError('');
    setIsLoading(true);

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: account.email,
          password: account.password,
        }),
      });

      if (!response.ok) {
        throw new Error('Login failed.');
      }

      const user = (await response.json()) as LoginUser;

      localStorage.setItem('opsflow.user', JSON.stringify(user));
      localStorage.setItem(
        'opsflow.credentials',
        btoa(`${account.email}:${account.password}`),
      );

      onLogin(user);
      navigate(redirectTo);
    } catch {
      setError('Unable to log in with that demo account.');
    } finally {
      setIsLoading(false);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);

    await loginWithAccount({
      label: 'Manual Login',
      email: String(formData.get('email')),
      password: String(formData.get('password')),
    });
  }

  return (
    <main className="centered-page">
      <section className="auth-card">
        <h1>Login</h1>
        <p>Select a demo role or enter credentials manually.</p>

        <div className="demo-login-grid">
          {DEMO_ACCOUNTS.map((account) => (
            <button
              key={account.email}
              type="button"
              onClick={() => loginWithAccount(account)}
              disabled={isLoading}
            >
              {account.label}
            </button>
          ))}
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <label>
            Email
            <input name="email" type="email" required />
          </label>

          <label>
            Password
            <input name="password" type="password" required />
          </label>

          <button type="submit" disabled={isLoading}>
            Login
          </button>
        </form>

        {error ? <p className="error-message">{error}</p> : null}
      </section>
    </main>
  );
}



function TicketDetailPage() {
  const { ticketId } = useParams();
  const [ticket, setTicket] = useState<TicketResponse | null>(null);
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isActive = true;

    async function loadTicket() {
      setError('');
      setIsLoading(true);

      try {
        const response = await fetch(`/api/tickets/${ticketId}`, {
          headers: {
            ...getAuthHeader(),
          },
        });

        if (!response.ok) {
          throw new Error('Unable to load ticket.');
        }

        const data = (await response.json()) as TicketResponse;

        if (isActive) {
          setTicket(data);
        }
      } catch {
        if (isActive) {
          setError('Unable to load ticket.');
        }
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    }

    loadTicket();

    return () => {
      isActive = false;
    };
  }, [ticketId]);

  return (
    <main className="page-container">
      <section className="content-card">
        <div className="page-title-row">
          <div>
            <h1>Ticket Detail</h1>
            <p>Review ticket information, priority, status, and SLA timing.</p>
          </div>

          <Link className="primary-link" to="/tickets">
            Back to Tickets
          </Link>
        </div>

        {isLoading ? <p>Loading ticket...</p> : null}

        {error ? <p className="error-message">{error}</p> : null}

        {!isLoading && !error && ticket ? (
          <div className="detail-layout">
            <section className="detail-section">
              <h2>{ticket.ticketNumber}</h2>
              <p className="detail-title">{ticket.title}</p>
              <p>{ticket.description}</p>
            </section>

            <section className="detail-grid">
              <div>
                <span className="detail-label">Affected System</span>
                <strong>{ticket.affectedSystem}</strong>
              </div>

              <div>
                <span className="detail-label">Impact</span>
                <strong>{ticket.impact}</strong>
              </div>

              <div>
                <span className="detail-label">Urgency</span>
                <strong>{ticket.urgency}</strong>
              </div>

              <div>
                <span className="detail-label">Priority</span>
                <strong>{ticket.priority}</strong>
              </div>

              <div>
                <span className="detail-label">Status</span>
                <strong>{ticket.status}</strong>
              </div>

              <div>
                <span className="detail-label">SLA Breached</span>
                <strong>{ticket.slaBreached ? 'Yes' : 'No'}</strong>
              </div>

              <div>
                <span className="detail-label">Requester</span>
                <strong>{ticket.requesterDisplayName}</strong>
                <span>{ticket.requesterEmail}</span>
              </div>

              <div>
                <span className="detail-label">Assigned Analyst</span>
                <strong>{ticket.assignedToDisplayName ?? 'Unassigned'}</strong>
                {ticket.assignedToEmail ? <span>{ticket.assignedToEmail}</span> : null}
              </div>

              <div>
                <span className="detail-label">Created</span>
                <strong>{new Date(ticket.createdAt).toLocaleString()}</strong>
              </div>

              <div>
                <span className="detail-label">Updated</span>
                <strong>{new Date(ticket.updatedAt).toLocaleString()}</strong>
              </div>

              <div>
                <span className="detail-label">SLA Due</span>
                <strong>{new Date(ticket.slaDueAt).toLocaleString()}</strong>
              </div>

              <div>
                <span className="detail-label">Resolved</span>
                <strong>{ticket.resolvedAt ? new Date(ticket.resolvedAt).toLocaleString() : 'Not resolved'}</strong>
              </div>

              <div>
                <span className="detail-label">Closed</span>
                <strong>{ticket.closedAt ? new Date(ticket.closedAt).toLocaleString() : 'Not closed'}</strong>
              </div>
            </section>
          </div>
        ) : null}
      </section>
    </main>
  );
}

function MyTicketsPage({ user }: { user: LoginUser | null }) {
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isActive = true;

    async function loadTickets() {
      setError('');
      setIsLoading(true);

      try {
        const response = await fetch('/api/tickets', {
          headers: {
            ...getAuthHeader(),
          },
        });

        if (!response.ok) {
          throw new Error('Unable to load tickets.');
        }

        const data = (await response.json()) as TicketResponse[];

        if (isActive) {
          setTickets(data);
        }
      } catch {
        if (isActive) {
          setError('Unable to load tickets.');
        }
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    }

    loadTickets();

    return () => {
      isActive = false;
    };
  }, []);

  if (!hasRole(user, 'REQUESTER')) {
    return (
      <main className="centered-page">
        <section className="hero-card">
          <h1>Access Denied</h1>
          <p>Only requesters can view My Tickets.</p>
        </section>
      </main>
    );
  }

  return (
    <main className="page-container">
      <section className="content-card">
        <div className="page-title-row">
          <div>
            <h1>My Tickets</h1>
            <p>View tickets you have submitted for support.</p>
          </div>

          <Link className="primary-link" to="/tickets/new">
            Create Ticket
          </Link>
        </div>

        {isLoading ? <p>Loading tickets...</p> : null}

        {error ? <p className="error-message">{error}</p> : null}

        {!isLoading && !error && tickets.length === 0 ? (
          <p>No tickets found.</p>
        ) : null}

        {!isLoading && !error && tickets.length > 0 ? (
          <div className="table-wrapper">
            <table className="ticket-table">
              <thead>
                <tr>
                  <th>Ticket</th>
                  <th>Title</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>SLA Due</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {tickets.map((ticket) => (
                  <tr key={ticket.id}>
                    <td>
                      <Link to={`/tickets/${ticket.id}`}>{ticket.ticketNumber}</Link>
                    </td>
                    <td>
                      <Link to={`/tickets/${ticket.id}`}>{ticket.title}</Link>
                    </td>
                    <td>{ticket.priority}</td>
                    <td>{ticket.status}</td>
                    <td>{new Date(ticket.slaDueAt).toLocaleString()}</td>
                    <td>{new Date(ticket.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>
    </main>
  );
}

function CreateTicketPage({ user }: { user: LoginUser | null }) {
  const [createdTicket, setCreatedTicket] = useState<TicketResponse | null>(null);
  const [error, setError] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!hasRole(user, 'REQUESTER')) {
    return (
      <main className="centered-page">
        <section className="hero-card">
          <h1>Access Denied</h1>
          <p>Only requesters can create tickets.</p>
        </section>
      </main>
    );
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const form = event.currentTarget;

    setCreatedTicket(null);
    setError('');
    setIsSubmitting(true);

    const formData = new FormData(form);

    const payload = {
      title: String(formData.get('title')),
      description: String(formData.get('description')),
      affectedSystem: String(formData.get('affectedSystem')),
      impact: String(formData.get('impact')),
      urgency: String(formData.get('urgency')),
    };

    try {
      const response = await fetch('/api/tickets', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader(),
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error('Ticket creation failed.');
      }

      const ticket = (await response.json()) as TicketResponse;
      setError('');
      setCreatedTicket(ticket);
      form.reset();
    } catch {
      setError('Unable to create ticket. Please check the form and try again.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="page-container">
      <section className="content-card">
        <h1>Create Ticket</h1>
        <p>Submit a new incident for triage and SLA tracking.</p>

        <form onSubmit={handleSubmit} className="ticket-form">
          <label>
            Title
            <input
              name="title"
              type="text"
              maxLength={160}
              required
              placeholder="Billing export failed"
            />
          </label>

          <label>
            Description
            <textarea
              name="description"
              maxLength={5000}
              required
              placeholder="Describe the incident, who is affected, and what error you are seeing."
            />
          </label>

          <label>
            Affected System
            <input
              name="affectedSystem"
              type="text"
              maxLength={120}
              required
              placeholder="Billing Portal"
            />
          </label>

          <div className="form-grid">
            <label>
              Impact
              <select name="impact" required defaultValue="MEDIUM">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </label>

            <label>
              Urgency
              <select name="urgency" required defaultValue="MEDIUM">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </label>
          </div>

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Creating Ticket...' : 'Create Ticket'}
          </button>
        </form>

        {createdTicket ? (
          <div className="success-message">
            <strong>Ticket {createdTicket.ticketNumber} created successfully.</strong>
            <span>Priority: {createdTicket.priority}</span>
            <span>Status: {createdTicket.status}</span>
            <span>SLA Due: {new Date(createdTicket.slaDueAt).toLocaleString()}</span>
          </div>
        ) : null}

        {error ? <p className="error-message">{error}</p> : null}
      </section>
    </main>
  );
}

function PlaceholderPage({ title }: { title: string }) {
  return (
    <main className="centered-page">
      <section className="hero-card">
        <h1>{title}</h1>
        <p>This page will be implemented in a later step.</p>
      </section>
    </main>
  );
}

export default App;
