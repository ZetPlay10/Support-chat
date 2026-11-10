import { useEffect, useState } from 'react'
import { api, errorMessage, fieldErrors } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { ConversationList } from '../components/ConversationList'

const TITLE = { USER: 'Conversatiile mele', AGENT: 'Conversatii preluate', ADMIN: 'Toate conversatiile' }

export function ConversationsPage() {
  const { user } = useAuth()
  const [conversations, setConversations] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    api
      .get('/conversations')
      .then((res) => setConversations(res.data))
      .catch((err) => setError(errorMessage(err)))
  }, [])

  return (
    <section className="card">
      <h1>{TITLE[user.role]}</h1>
      {/* doar clientii deschid conversatii (POST /api/conversations -> 403 pentru ceilalti) */}
      {user.role === 'USER' && (
        <NewConversationForm onCreated={(c) => setConversations((list) => [c, ...(list ?? [])])} />
      )}
      {error && <p className="alert" role="alert">{error}</p>}
      {conversations === null && !error && <p className="muted">Se incarca...</p>}
      {conversations && <ConversationList conversations={conversations} empty="Nu exista conversatii." />}
    </section>
  )
}

function NewConversationForm({ onCreated }) {
  const [subject, setSubject] = useState('')
  const [error, setError] = useState(null)
  const [sending, setSending] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSending(true)
    setError(null)
    try {
      const { data } = await api.post('/conversations', { subject: subject.trim() })
      onCreated(data)
      setSubject('')
    } catch (err) {
      setError(fieldErrors(err).subject ?? errorMessage(err))
    } finally {
      setSending(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="inline-form">
      <label htmlFor="subject" className="sr-only">Subiect</label>
      <input id="subject" placeholder="Cu ce va putem ajuta?" required maxLength={200}
        value={subject} onChange={(e) => setSubject(e.target.value)} />
      <button type="submit" disabled={sending}>Deschide conversatie</button>
      {error && <span className="field-error">{error}</span>}
    </form>
  )
}
