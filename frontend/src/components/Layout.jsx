import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const ROLE_LABEL = { USER: 'Client', AGENT: 'Agent', ADMIN: 'Admin' }

export function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <>
      <header className="topbar">
        <NavLink to="/" className="brand">Support Chat</NavLink>
        {user && (
          <nav className="nav">
            <NavLink to="/conversatii">Conversatii</NavLink>
            {(user.role === 'AGENT' || user.role === 'ADMIN') && <NavLink to="/coada">Coada</NavLink>}
          </nav>
        )}
        <div className="account">
          {user ? (
            <>
              <span>
                {user.displayName} <span className="badge">{ROLE_LABEL[user.role]}</span>
              </span>
              <button type="button" className="link" onClick={handleLogout}>Iesire</button>
            </>
          ) : (
            <>
              <NavLink to="/login">Autentificare</NavLink>
              <NavLink to="/inregistrare">Cont nou</NavLink>
            </>
          )}
        </div>
      </header>
      <main className="container">
        <Outlet />
      </main>
    </>
  )
}
