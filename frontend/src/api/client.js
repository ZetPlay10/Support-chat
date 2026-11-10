import axios from 'axios'
import { clearToken, loadToken } from '../auth/tokenStore'

// Toate cererile merg la /api; in dezvoltare Vite le trimite (proxy) catre Spring Boot pe 8080
export const api = axios.create({ baseURL: '/api' })

// Adauga "Authorization: Bearer <token>" la fiecare cerere, daca suntem logati
api.interceptors.request.use((config) => {
  const token = loadToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token.accessToken}`
  }
  return config
})

// 401 pe o cerere cu token = token expirat sau invalid -> delogare.
// (401 la /auth/login inseamna doar parola gresita, il trateaza formularul.)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const sentToken = Boolean(error.config?.headers?.Authorization)
    if (error.response?.status === 401 && sentToken) {
      clearToken()
      window.dispatchEvent(new Event('auth:expired'))
    }
    return Promise.reject(error)
  },
)

// Backend-ul intoarce erori ProblemDetail: {title, detail, status, errors?: {camp: mesaj}}
export function errorMessage(error) {
  const problem = error.response?.data
  if (problem?.detail) return problem.detail
  if (!error.response) return 'Serverul nu raspunde. Verificati conexiunea.'
  return 'A aparut o eroare neasteptata.'
}

export function fieldErrors(error) {
  return error.response?.data?.errors ?? {}
}
