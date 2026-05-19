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

type TicketCommentResponse = {
  id: number;
  ticketId: number;
  authorId: number;
  authorEmail: string;
  authorDisplayName: string;
  body: string;
  internal: boolean;
  createdAt: string;
};

type TicketAuditEventResponse = {
  id: number;
  ticketId: number;
  actorId: number;
  actorEmail: string;
  actorDisplayName: string;
  eventType: string;
  oldValue: string | null;
  newValue: string | null;
  message: string;
  createdAt: string;
};

type TriageSuggestionResponse = {
  suggestedImpact: string;
  suggestedUrgency: string;
  suggestedPriority: string;
  explanation: string;
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
  slaRisk: string;
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

function formatEnumLabel(value: string): string {
  return value.replaceAll('_', ' ');
}

function getSlaRiskClassName(slaRisk: string): string {
  return `risk-badge risk-${slaRisk.toLowerCase().replaceAll('_', '-')}`;
}

function SlaRiskBadge({ slaRisk }: { slaRisk: string }) {
  return <span className={getSlaRiskClassName(slaRisk)}>{formatEnumLabel(slaRisk)}</span>;
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
              <TicketDetailPage user={user} />
            </ProtectedRoute>
          }
        />

        <Route
          path="/queue"
          element={
            <ProtectedRoute user={user}>
              <AnalystQueuePage user={user} />
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




function AnalystQueuePage({ user }: { user: LoginUser | null }) {
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [error, setError] = useState<string>('');
  const [claimMessage, setClaimMessage] = useState<string>('');
  const [claimError, setClaimError] = useState<string>('');
  const [claimingTicketId, setClaimingTicketId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);

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
        throw new Error('Unable to load queue.');
      }

      const data = (await response.json()) as TicketResponse[];
      setTickets(data);
    } catch {
      setError('Unable to load analyst queue.');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadTickets();
  }, []);

  async function handleClaimTicket(ticket: TicketResponse) {
    setClaimMessage('');
    setClaimError('');
    setClaimingTicketId(ticket.id);

    try {
      const response = await fetch(`/api/tickets/${ticket.id}/claim`, {
        method: 'PATCH',
        headers: {
          ...getAuthHeader(),
        },
      });

      if (!response.ok) {
        throw new Error('Unable to claim ticket.');
      }

      const claimedTicket = (await response.json()) as TicketResponse;

      setTickets((currentTickets) =>
        currentTickets.map((currentTicket) =>
          currentTicket.id === claimedTicket.id ? claimedTicket : currentTicket,
        ),
      );

      setClaimMessage(`Claimed ${claimedTicket.ticketNumber}.`);
    } catch {
      setClaimError('Unable to claim ticket.');
    } finally {
      setClaimingTicketId(null);
    }
  }

  if (!hasRole(user, 'ANALYST')) {
    return (
      <main className="centered-page">
        <section className="hero-card">
          <h1>Access Denied</h1>
          <p>Only analysts can view the queue.</p>
        </section>
      </main>
    );
  }

  const unassignedTickets = tickets.filter((ticket) => ticket.assignedToEmail === null);
  const assignedToMeTickets = tickets.filter(
    (ticket) => ticket.assignedToEmail === user?.email,
  );
  const overdueTickets = tickets.filter(isTicketOverdue);
  const nearingSlaTickets = tickets.filter(isTicketNearingSla);

  return (
    <main className="page-container">
      <section className="content-card">
        <div className="page-title-row">
          <div>
            <h1>Analyst Queue</h1>
            <p>Review unassigned work, assigned tickets, and SLA risk.</p>
          </div>

          <Link className="primary-link" to="/">
            Back
          </Link>
        </div>

        {isLoading ? <p>Loading queue...</p> : null}

        {error ? <p className="error-message">{error}</p> : null}

        {claimMessage ? <p className="success-text">{claimMessage}</p> : null}
        {claimError ? <p className="error-message">{claimError}</p> : null}

        {!isLoading && !error ? (
          <div className="queue-layout">
            <QueueSection
              title="Unassigned Tickets"
              description="Tickets available for analyst ownership."
              tickets={unassignedTickets}
              showClaimButton
              claimingTicketId={claimingTicketId}
              onClaimTicket={handleClaimTicket}
            />

            <QueueSection
              title="Assigned to Me"
              description="Tickets currently assigned to your analyst account."
              tickets={assignedToMeTickets}
            />

            <QueueSection
              title="Nearing SLA"
              description="Tickets due soon and not yet resolved."
              tickets={nearingSlaTickets}
            />

            <QueueSection
              title="Overdue Tickets"
              description="Tickets past their SLA due time."
              tickets={overdueTickets}
            />
          </div>
        ) : null}
      </section>
    </main>
  );
}

function QueueSection({
  title,
  description,
  tickets,
  showClaimButton = false,
  claimingTicketId = null,
  onClaimTicket,
}: {
  title: string;
  description: string;
  tickets: TicketResponse[];
  showClaimButton?: boolean;
  claimingTicketId?: number | null;
  onClaimTicket?: (ticket: TicketResponse) => void;
}) {
  return (
    <section className="queue-section">
      <div className="queue-section-header">
        <div>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>

        <span className="queue-count">{tickets.length}</span>
      </div>

      {tickets.length === 0 ? (
        <p>No tickets in this section.</p>
      ) : (
        <div className="table-wrapper">
          <table className="ticket-table">
            <thead>
              <tr>
                <th>Ticket</th>
                <th>Title</th>
                <th>Priority</th>
                <th>Status</th>
                <th>SLA Due</th>
                {showClaimButton ? <th>Action</th> : null}
              </tr>
            </thead>
            <tbody>
              {tickets.map((ticket) => (
                <tr key={`${title}-${ticket.id}`}>
                  <td>
                    <Link to={`/tickets/${ticket.id}`}>{ticket.ticketNumber}</Link>
                  </td>
                  <td>
                    <Link to={`/tickets/${ticket.id}`}>{ticket.title}</Link>
                  </td>
                  <td>{ticket.priority}</td>
                  <td>{ticket.status}</td>
                  <td><SlaRiskBadge slaRisk={ticket.slaRisk} /></td>
                  <td>{new Date(ticket.slaDueAt).toLocaleString()}</td>
                  {showClaimButton ? (
                    <td>
                      <button
                        type="button"
                        className="small-action-button"
                        disabled={claimingTicketId === ticket.id}
                        onClick={() => onClaimTicket?.(ticket)}
                      >
                        {claimingTicketId === ticket.id ? 'Claiming...' : 'Claim'}
                      </button>
                    </td>
                  ) : null}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

function isTicketClosedOrResolved(ticket: TicketResponse): boolean {
  return ticket.status === 'RESOLVED' || ticket.status === 'CLOSED';
}

function isTicketOverdue(ticket: TicketResponse): boolean {
  return !isTicketClosedOrResolved(ticket) && new Date(ticket.slaDueAt).getTime() < Date.now();
}

function isTicketNearingSla(ticket: TicketResponse): boolean {
  const dueAt = new Date(ticket.slaDueAt).getTime();
  const now = Date.now();
  const fourHoursFromNow = now + 4 * 60 * 60 * 1000;

  return !isTicketClosedOrResolved(ticket) && dueAt >= now && dueAt <= fourHoursFromNow;
}


function TicketDetailPage({ user }: { user: LoginUser | null }) {
  const { ticketId } = useParams();
  const [ticket, setTicket] = useState<TicketResponse | null>(null);
  const [comments, setComments] = useState<TicketCommentResponse[]>([]);
  const [auditEvents, setAuditEvents] = useState<TicketAuditEventResponse[]>([]);
  const [error, setError] = useState<string>('');
  const [commentError, setCommentError] = useState<string>('');
  const [commentMessage, setCommentMessage] = useState<string>('');
  const [statusUpdateError, setStatusUpdateError] = useState<string>('');
  const [statusUpdateMessage, setStatusUpdateMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);
  const [isAddingComment, setIsAddingComment] = useState(false);
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);

  const canUpdateStatus = hasRole(user, 'ANALYST') || hasRole(user, 'MANAGER');
  const canCreateInternalComment = hasRole(user, 'ANALYST') || hasRole(user, 'MANAGER');

  async function loadTicketAndComments() {
    setError('');
    setIsLoading(true);

    try {
      const [ticketResponse, commentsResponse, auditEventsResponse] = await Promise.all([
        fetch(`/api/tickets/${ticketId}`, {
          headers: {
            ...getAuthHeader(),
          },
        }),
        fetch(`/api/tickets/${ticketId}/comments`, {
          headers: {
            ...getAuthHeader(),
          },
        }),
        fetch(`/api/tickets/${ticketId}/audit-events`, {
          headers: {
            ...getAuthHeader(),
          },
        }),
      ]);

      if (!ticketResponse.ok || !commentsResponse.ok || !auditEventsResponse.ok) {
        throw new Error('Unable to load ticket.');
      }

      const ticketData = (await ticketResponse.json()) as TicketResponse;
      const commentsData = (await commentsResponse.json()) as TicketCommentResponse[];
      const auditEventsData = (await auditEventsResponse.json()) as TicketAuditEventResponse[];

      setTicket(ticketData);
      setComments(commentsData);
      setAuditEvents(auditEventsData);
    } catch {
      setError('Unable to load ticket.');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadTicketAndComments();
  }, [ticketId]);

  async function handleAddComment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!ticket) {
      return;
    }

    const form = event.currentTarget;
    const formData = new FormData(form);
    const body = String(formData.get('body'));
    const internal = canCreateInternalComment && formData.get('internal') === 'on';

    setCommentError('');
    setCommentMessage('');
    setIsAddingComment(true);

    try {
      const response = await fetch(`/api/tickets/${ticket.id}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader(),
        },
        body: JSON.stringify({
          body,
          internal,
        }),
      });

      if (!response.ok) {
        throw new Error('Unable to add comment.');
      }

      const newComment = (await response.json()) as TicketCommentResponse;

      setComments((currentComments) => [...currentComments, newComment]);
      setCommentMessage('Comment added.');
      form.reset();
    } catch {
      setCommentError('Unable to add comment.');
    } finally {
      setIsAddingComment(false);
    }
  }

  async function handleStatusUpdate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!ticket) {
      return;
    }

    setStatusUpdateError('');
    setStatusUpdateMessage('');
    setIsUpdatingStatus(true);

    const formData = new FormData(event.currentTarget);
    const nextStatus = String(formData.get('status'));

    try {
      const response = await fetch(`/api/tickets/${ticket.id}/status`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader(),
        },
        body: JSON.stringify({
          status: nextStatus,
        }),
      });

      if (!response.ok) {
        throw new Error('Unable to update status.');
      }

      const updatedTicket = (await response.json()) as TicketResponse;
      setTicket(updatedTicket);
      setStatusUpdateMessage(`Status updated to ${updatedTicket.status}.`);
    } catch {
      setStatusUpdateError('Unable to update status. Check that the transition is allowed.');
    } finally {
      setIsUpdatingStatus(false);
    }
  }

  return (
    <main className="page-container">
      <section className="content-card">
        <div className="page-title-row">
          <div>
            <h1>Ticket Detail</h1>
            <p>Review ticket information, priority, status, comments, and SLA timing.</p>
          </div>

          <Link className="primary-link" to={hasRole(user, 'ANALYST') ? '/queue' : '/tickets'}>
            Back
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

            {canUpdateStatus ? (
              <section className="status-update-card">
                <h2>Update Status</h2>
                <p>Move this ticket through the approved workflow.</p>

                <form onSubmit={handleStatusUpdate} className="status-update-form">
                  <label>
                    New Status
                    <select name="status" defaultValue={ticket.status}>
                      <option value="NEW">New</option>
                      <option value="TRIAGED">Triaged</option>
                      <option value="ASSIGNED">Assigned</option>
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="WAITING_ON_USER">Waiting on User</option>
                      <option value="RESOLVED">Resolved</option>
                      <option value="CLOSED">Closed</option>
                      <option value="REOPENED">Reopened</option>
                    </select>
                  </label>

                  <button type="submit" disabled={isUpdatingStatus}>
                    {isUpdatingStatus ? 'Updating...' : 'Update Status'}
                  </button>
                </form>

                {statusUpdateMessage ? <p className="success-text">{statusUpdateMessage}</p> : null}
                {statusUpdateError ? <p className="error-message">{statusUpdateError}</p> : null}
              </section>
            ) : null}

            <section className="comments-card">
              <h2>Comments</h2>

              <form onSubmit={handleAddComment} className="comment-form">
                <label>
                  Add Comment
                  <textarea
                    name="body"
                    maxLength={5000}
                    required
                    placeholder="Add a status update or question..."
                  />
                </label>

                {canCreateInternalComment ? (
                  <label className="checkbox-label">
                    <input name="internal" type="checkbox" />
                    Internal comment
                  </label>
                ) : null}

                <button type="submit" disabled={isAddingComment}>
                  {isAddingComment ? 'Adding...' : 'Add Comment'}
                </button>
              </form>

              {commentMessage ? <p className="success-text">{commentMessage}</p> : null}
              {commentError ? <p className="error-message">{commentError}</p> : null}

              <div className="comments-list">
                {comments.length === 0 ? (
                  <p>No comments yet.</p>
                ) : (
                  comments.map((comment) => (
                    <article key={comment.id} className="comment-item">
                      <div className="comment-header">
                        <strong>{comment.authorDisplayName}</strong>
                        <span>{new Date(comment.createdAt).toLocaleString()}</span>
                        {comment.internal ? <span className="internal-badge">Internal</span> : null}
                      </div>
                      <p>{comment.body}</p>
                    </article>
                  ))
                )}
              </div>
            </section>

            <section className="audit-card">
              <h2>Audit Trail</h2>

              {auditEvents.length === 0 ? (
                <p>No audit events yet.</p>
              ) : (
                <div className="audit-list">
                  {auditEvents.map((event) => (
                    <article key={event.id} className="audit-item">
                      <div className="audit-header">
                        <strong>{event.eventType.replaceAll('_', ' ')}</strong>
                        <span>{new Date(event.createdAt).toLocaleString()}</span>
                      </div>

                      <p>{event.message}</p>

                      <div className="audit-meta">
                        <span>Actor: {event.actorDisplayName}</span>
                        {event.oldValue ? <span>From: {event.oldValue}</span> : null}
                        {event.newValue ? <span>To: {event.newValue}</span> : null}
                      </div>
                    </article>
                  ))}
                </div>
              )}
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
                <span className="detail-label">SLA Risk</span>
                <strong><SlaRiskBadge slaRisk={ticket.slaRisk} /></strong>
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
                  <th>SLA Risk</th>
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
                    <td><SlaRiskBadge slaRisk={ticket.slaRisk} /></td>
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
  const [triageSuggestion, setTriageSuggestion] = useState<TriageSuggestionResponse | null>(null);
  const [selectedImpact, setSelectedImpact] = useState('MEDIUM');
  const [selectedUrgency, setSelectedUrgency] = useState('MEDIUM');
  const [error, setError] = useState<string>('');
  const [suggestionError, setSuggestionError] = useState<string>('');
  const [suggestionMessage, setSuggestionMessage] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSuggesting, setIsSuggesting] = useState(false);

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

  function getTicketPayload(form: HTMLFormElement) {
    const formData = new FormData(form);

    return {
      title: String(formData.get('title')),
      description: String(formData.get('description')),
      affectedSystem: String(formData.get('affectedSystem')),
      impact: selectedImpact,
      urgency: selectedUrgency,
    };
  }

  async function handleSuggestTriage(form: HTMLFormElement) {
    const payload = getTicketPayload(form);

    setCreatedTicket(null);
    setTriageSuggestion(null);
    setSuggestionError('');
    setSuggestionMessage('');
    setIsSuggesting(true);

    try {
      const response = await fetch('/api/tickets/triage-suggestion', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader(),
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error('Triage suggestion failed.');
      }

      const suggestion = (await response.json()) as TriageSuggestionResponse;
      setTriageSuggestion(suggestion);
      setSuggestionMessage('Suggestion generated. You can apply it or keep your current selections.');
    } catch {
      setSuggestionError('Unable to generate triage suggestion. Please check the form and try again.');
    } finally {
      setIsSuggesting(false);
    }
  }

  function handleApplySuggestion() {
    if (!triageSuggestion) {
      return;
    }

    setSelectedImpact(triageSuggestion.suggestedImpact);
    setSelectedUrgency(triageSuggestion.suggestedUrgency);
    setSuggestionMessage('Suggestion applied to Impact and Urgency. You can still change them before submitting.');
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const form = event.currentTarget;

    setCreatedTicket(null);
    setError('');
    setIsSubmitting(true);

    const payload = getTicketPayload(form);

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
      setTriageSuggestion(null);
      setSuggestionMessage('');
      setSelectedImpact('MEDIUM');
      setSelectedUrgency('MEDIUM');
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
              <select
                name="impact"
                required
                value={selectedImpact}
                onChange={(event) => setSelectedImpact(event.target.value)}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </label>

            <label>
              Urgency
              <select
                name="urgency"
                required
                value={selectedUrgency}
                onChange={(event) => setSelectedUrgency(event.target.value)}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </label>
          </div>

          <div className="form-actions">
            <button
              type="button"
              disabled={isSuggesting}
              onClick={(event) => {
                const form = event.currentTarget.form;
                if (form) {
                  handleSuggestTriage(form);
                }
              }}
            >
              {isSuggesting ? 'Generating Suggestion...' : 'Get Triage Suggestion'}
            </button>

            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Creating Ticket...' : 'Create Ticket'}
            </button>
          </div>
        </form>

        {triageSuggestion ? (
          <div className="triage-suggestion-card">
            <h2>Triage Suggestion</h2>
            <div className="suggestion-grid">
              <span>Impact: <strong>{triageSuggestion.suggestedImpact}</strong></span>
              <span>Urgency: <strong>{triageSuggestion.suggestedUrgency}</strong></span>
              <span>Priority: <strong>{triageSuggestion.suggestedPriority}</strong></span>
            </div>
            <p>{triageSuggestion.explanation}</p>
            <p>This suggestion is optional. You can apply it or keep your current selections.</p>

            <button type="button" onClick={handleApplySuggestion}>
              Apply Suggestion
            </button>
          </div>
        ) : null}

        {suggestionMessage ? <p className="success-text">{suggestionMessage}</p> : null}
        {suggestionError ? <p className="error-message">{suggestionError}</p> : null}

        {createdTicket ? (
          <div className="success-message">
            <strong>Ticket {createdTicket.ticketNumber} created successfully.</strong>
            <span>Priority: {createdTicket.priority}</span>
            <span>Status: {createdTicket.status}</span>
            <span>SLA Risk: <SlaRiskBadge slaRisk={createdTicket.slaRisk} /></span>
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
