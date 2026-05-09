import { FormEvent, ReactNode, useState } from 'react';
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
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
              <PlaceholderPage title="My Tickets" />
            </ProtectedRoute>
          }
        />

        <Route
          path="/tickets/new"
          element={
            <ProtectedRoute user={user}>
              <PlaceholderPage title="Create Ticket" />
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
