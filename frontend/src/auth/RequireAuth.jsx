import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

// Protejeaza o ruta: anonim -> /login (apoi inapoi aici), rol nepotrivit -> mesaj 403.
// Doar ascunde interfata; protectia reala o face backend-ul (401/403 pe API).
export function RequireAuth({ roles, children }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) return <p className="muted">Se incarca...</p>
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />
  if (roles && !roles.includes(user.role)) {
    return (
      <section className="card">
        <h1>Acces interzis</h1>
        <p>Contul dumneavoastra nu are acces la aceasta pagina.</p>
      </section>
    )
  }
  return children
}
