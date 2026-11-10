import { useCallback, useEffect, useMemo, useState } from 'react'
import { api } from '../api/client'
import { AuthContext } from './AuthContext'
import { clearToken, loadToken, saveToken } from './tokenStore'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  // true pana aflam daca token-ul salvat mai e valid (evita un redirect gresit la /login)
  const [loading, setLoading] = useState(() => loadToken() !== null)
  const [expiresAt, setExpiresAt] = useState(() => loadToken()?.expiresAt ?? null)

  const logout = useCallback(() => {
    clearToken()
    setUser(null)
    setExpiresAt(null)
  }, [])

  // La pornire: daca avem un token salvat, il verificam cu GET /api/auth/me
  useEffect(() => {
    if (!loadToken()) return
    api
      .get('/auth/me')
      .then((res) => setUser(res.data))
      .catch(() => logout())
      .finally(() => setLoading(false))
  }, [logout])

  // Interceptorul axios anunta un 401 (token expirat / invalid)
  useEffect(() => {
    window.addEventListener('auth:expired', logout)
    return () => window.removeEventListener('auth:expired', logout)
  }, [logout])

  // Delogare automata cand expira token-ul, chiar daca nu se face nicio cerere
  useEffect(() => {
    if (!expiresAt) return
    const timer = setTimeout(logout, Math.max(0, expiresAt - Date.now()))
    return () => clearTimeout(timer)
  }, [expiresAt, logout])

  const login = useCallback(async (username, password) => {
    const { data } = await api.post('/auth/login', { username, password })
    const saved = saveToken(data.accessToken, data.expiresIn)
    setExpiresAt(saved.expiresAt)
    setUser(data.user)
    return data.user
  }, [])

  // Inregistrarea creeaza doar clienti (USER); dupa ea facem direct login
  const register = useCallback(
    async (form) => {
      await api.post('/auth/register', form)
      return login(form.username, form.password)
    },
    [login],
  )

  const value = useMemo(
    () => ({ user, loading, login, register, logout }),
    [user, loading, login, register, logout],
  )
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
