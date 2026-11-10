import { useEffect, useState } from 'react'
import { api, errorMessage } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { ConversationList } from '../components/ConversationList'

// Conversatiile care asteapta un agent (GET /api/conversations/queue: doar AGENT si ADMIN)
export function QueuePage() {
  const { user } = useAuth()
  const [queue, setQueue] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    api
      .get('/conversations/queue')
      .then((res) => setQueue(res.data))
      .catch((err) => setError(errorMessage(err)))
  }, [])

  async function assign(id) {
    setError(null)
    try {
      await api.post(`/conversations/${id}/assign`)
    } catch (err) {
      // 409: alt agent a preluat-o intre timp; o scoatem oricum din lista
      setError(errorMessage(err))
    }
    setQueue((list) => list.filter((c) => c.id !== id))
  }

  return (
    <section className="card">
      <h1>Coada de asteptare</h1>
      {error && <p className="alert" role="alert">{error}</p>}
      {queue === null && !error && <p className="muted">Se incarca...</p>}
      {queue && (
        <ConversationList
          conversations={queue}
          empty="Nicio conversatie nu asteapta."
          // doar agentii preiau conversatii (adminul doar vede coada)
          action={user.role === 'AGENT' ? (c) => <button type="button" onClick={() => assign(c.id)}>Preia</button> : undefined}
        />
      )}
    </section>
  )
}
