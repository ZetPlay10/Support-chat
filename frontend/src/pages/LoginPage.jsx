import { useState } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { errorMessage } from '../api/client'
import { homeFor, useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const { user, login } = useAuth()
  const location = useLocation()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState(null)
  const [sending, setSending] = useState(false)

  // dupa login: inapoi la pagina ceruta inainte (RequireAuth o pune in state), altfel pagina rolului
  if (user) return <Navigate to={location.state?.from?.pathname ?? homeFor(user)} replace />

  async function handleSubmit(e) {
    e.preventDefault()
    setSending(true)
    setError(null)
    try {
      await login(form.username.trim(), form.password)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSending(false)
    }
  }

  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  return (
    <section className="card narrow">
      <h1>Autentificare</h1>
      {error && <p className="alert" role="alert">{error}</p>}
      <form onSubmit={handleSubmit} className="form">
        <label htmlFor="username">Username</label>
        <input id="username" name="username" autoComplete="username" required
          value={form.username} onChange={update} />

        <label htmlFor="password">Parola</label>
        <input id="password" name="password" type="password" autoComplete="current-password" required
          value={form.password} onChange={update} />

        <button type="submit" disabled={sending}>{sending ? 'Se verifica...' : 'Intra in cont'}</button>
      </form>
      <p className="muted">Nu aveti cont? <Link to="/inregistrare">Creati unul</Link></p>
    </section>
  )
}
