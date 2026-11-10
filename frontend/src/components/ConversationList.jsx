const STATUS_LABEL = { WAITING: 'In asteptare', ACTIVE: 'Activa', CLOSED: 'Inchisa' }

const formatDate = (iso) =>
  new Date(iso).toLocaleString('ro-RO', { dateStyle: 'short', timeStyle: 'short' })

// action(conversation) -> buton optional afisat pe fiecare rand
export function ConversationList({ conversations, empty, action }) {
  if (conversations.length === 0) return <p className="muted">{empty}</p>
  return (
    <ul className="list">
      {conversations.map((c) => (
        <li key={c.id} className="row">
          <div>
            <strong>{c.subject}</strong>
            <div className="muted small">
              #{c.id} · {c.client.displayName} · {formatDate(c.createdAt)}
              {c.agent && <> · agent: {c.agent.displayName}</>}
            </div>
          </div>
          <div className="row-end">
            <span className={`status status-${c.status.toLowerCase()}`}>{STATUS_LABEL[c.status]}</span>
            {action?.(c)}
          </div>
        </li>
      ))}
    </ul>
  )
}
