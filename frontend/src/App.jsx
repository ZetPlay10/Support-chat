import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { homeFor, useAuth } from './auth/AuthContext'
import { AuthProvider } from './auth/AuthProvider'
import { RequireAuth } from './auth/RequireAuth'
import { Layout } from './components/Layout'
import { ConversationsPage } from './pages/ConversationsPage'
import { LoginPage } from './pages/LoginPage'
import { QueuePage } from './pages/QueuePage'
import { RegisterPage } from './pages/RegisterPage'

function Home() {
  const { user, loading } = useAuth()
  if (loading) return null
  return <Navigate to={user ? homeFor(user) : '/login'} replace />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<Home />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="inregistrare" element={<RegisterPage />} />
            <Route path="conversatii" element={<RequireAuth><ConversationsPage /></RequireAuth>} />
            <Route path="coada" element={<RequireAuth roles={['AGENT', 'ADMIN']}><QueuePage /></RequireAuth>} />
            <Route path="*" element={<section className="card"><h1>Pagina inexistenta</h1></section>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
