import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { errorMessage, fieldErrors } from '../api/client'
import { homeFor, useAuth } from '../auth/AuthContext'

const EMPTY = { username: '', email: '', displayName: '', password: '', confirm: '' }

export function RegisterPage() {
  const { user, register } = useAuth()
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [error, setError] = useState(null)
  const [sending, setSending] = useState(false)

  // dupa inregistrare + login, user devine setat si redirectionam
  if (user) return <Navigate to={homeFor(user)} replace />

  async function handleSubmit(e) {
    e.preventDefault()
    if (form.password !== form.confirm) {
      setErrors({ confirm: 'Parolele nu coincid' })
      return
    }
    setSending(true)
    setErrors({})
    setError(null)
    try {
      const { confirm: _confirm, ...request } = form
      await register(request)
    } catch (err) {
      // 400: mesaje pe campuri din "errors"; 409: username / email deja folosit
      setErrors(fieldErrors(err))
      setError(errorMessage(err))
    } finally {
      setSending(false)
    }
  }

  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const field = (name, label, props = {}) => (
    <>
      <label htmlFor={name}>{label}</label>
      <input id={name} name={name} value={form[name]} onChange={update} required
        aria-invalid={Boolean(errors[name])} {...props} />
      {errors[name] && <span className="field-error">{errors[name]}</span>}
    </>
  )

  return (
    <section className="card narrow">
      <h1>Cont nou</h1>
      {error && <p className="alert" role="alert">{error}</p>}
      <form onSubmit={handleSubmit} className="form">
        {field('username', 'Username', { autoComplete: 'username', minLength: 3, maxLength: 50 })}
        {field('email', 'Email', { type: 'email', autoComplete: 'email' })}
        {field('displayName', 'Nume afisat', { autoComplete: 'name', maxLength: 100 })}
        {field('password', 'Parola (minim 8 caractere)', { type: 'password', autoComplete: 'new-password', minLength: 8, maxLength: 72 })}
        {field('confirm', 'Confirmati parola', { type: 'password', autoComplete: 'new-password' })}
        <button type="submit" disabled={sending}>{sending ? 'Se creeaza...' : 'Creeaza contul'}</button>
      </form>
      <p className="muted">Aveti deja cont? <Link to="/login">Autentificati-va</Link></p>
    </section>
  )
}
