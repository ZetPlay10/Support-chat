import { createContext, useContext } from 'react'

export const AuthContext = createContext(null)

// { user, loading, login, register, logout }
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth trebuie folosit in interiorul <AuthProvider>')
  return ctx
}

// Pagina de start difera dupa rol: agentul incepe cu coada de asteptare
export function homeFor(user) {
  return user?.role === 'AGENT' ? '/coada' : '/conversatii'
}
