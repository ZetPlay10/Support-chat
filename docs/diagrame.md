# Support Chat — Diagrame

Diagramele sunt scrise în [Mermaid](https://mermaid.js.org/) și se randează direct pe GitHub
sau în VS Code (extensia *Markdown Preview Mermaid Support*).

## 1. Arhitectura sistemului

```mermaid
flowchart LR
    subgraph Client["Browser"]
        UI["React + Vite<br/>(axios, STOMP client)"]
    end

    subgraph Server["Spring Boot (port 8080)"]
        SEC["Spring Security<br/>filtru JWT"]
        REST["REST Controllers<br/>/api/**"]
        WS["WebSocket / STOMP<br/>/ws"]
        SVC["Servicii<br/>(logică de business)"]
        REPO["Repository-uri<br/>Spring Data JPA"]
    end

    DB[("PostgreSQL")]

    UI -- "HTTP + JWT" --> SEC
    UI <-- "WebSocket (STOMP) + JWT" --> WS
    SEC --> REST
    REST --> SVC
    WS --> SVC
    SVC --> REPO
    REPO -- "JDBC / Hibernate" --> DB
```

## 2. Cazuri de utilizare

```mermaid
flowchart LR
    Client(["👤 Client"])
    Agent(["🎧 Agent suport"])
    Admin(["🛠️ Administrator"])

    subgraph Sistem["Support Chat"]
        UC1(["Înregistrare / Autentificare"])
        UC2(["Deschide conversație nouă"])
        UC3(["Trimite / primește mesaje în timp real"])
        UC4(["Vizualizează istoricul conversațiilor"])
        UC5(["Preia conversație din coada de așteptare"])
        UC6(["Închide conversația"])
        UC7(["Gestionează utilizatori și agenți"])
        UC8(["Vizualizează toate conversațiile"])
    end

    Client --- UC1
    Client --- UC2
    Client --- UC3
    Client --- UC4

    Agent --- UC1
    Agent --- UC3
    Agent --- UC4
    Agent --- UC5
    Agent --- UC6

    Admin --- UC1
    Admin --- UC7
    Admin --- UC8
```

## 3. Diagrama bazei de date (ER)

```mermaid
erDiagram
    USERS ||--o{ CONVERSATIONS : "deschide (client)"
    USERS |o--o{ CONVERSATIONS : "preia (agent)"
    CONVERSATIONS ||--o{ MESSAGES : "conține"
    USERS ||--o{ MESSAGES : "trimite"

    USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password_hash
        varchar display_name
        varchar role "USER | AGENT | ADMIN"
        boolean online
        timestamp created_at
    }

    CONVERSATIONS {
        bigint id PK
        varchar subject
        varchar status "WAITING | ACTIVE | CLOSED"
        bigint client_id FK
        bigint agent_id FK "null cât timp e în așteptare"
        timestamp created_at
        timestamp closed_at
    }

    MESSAGES {
        bigint id PK
        bigint conversation_id FK
        bigint sender_id FK
        text content
        timestamp sent_at
        boolean is_read
    }
```

## 4. Diagrama de clase (backend)

```mermaid
classDiagram
    direction LR

    class User {
        Long id
        String username
        String email
        String passwordHash
        String displayName
        Role role
        boolean online
        Instant createdAt
    }
    class Conversation {
        Long id
        String subject
        ConversationStatus status
        User client
        User agent
        Instant createdAt
        Instant closedAt
    }
    class Message {
        Long id
        Conversation conversation
        User sender
        String content
        Instant sentAt
        boolean read
    }
    class Role {
        <<enumeration>>
        USER
        AGENT
        ADMIN
    }
    class ConversationStatus {
        <<enumeration>>
        WAITING
        ACTIVE
        CLOSED
    }

    User "1" --> "*" Conversation : client
    User "0..1" --> "*" Conversation : agent
    Conversation "1" *-- "*" Message
    User "1" --> "*" Message : sender
    User ..> Role
    Conversation ..> ConversationStatus

    class AuthController {
        +register(RegisterRequest)
        +login(LoginRequest) AuthResponse
    }
    class ConversationController {
        +create(CreateConversationRequest)
        +myConversations()
        +waitingQueue()
        +assign(id)
        +close(id)
        +messages(id)
    }
    class ChatWebSocketController {
        +sendMessage(ChatMessageDto)
    }
    class AuthService
    class ConversationService
    class MessageService
    class JwtService {
        +generateToken(User) String
        +validateToken(String) boolean
    }
    class UserRepository {
        <<interface>>
    }
    class ConversationRepository {
        <<interface>>
    }
    class MessageRepository {
        <<interface>>
    }

    AuthController --> AuthService
    ConversationController --> ConversationService
    ChatWebSocketController --> MessageService
    AuthService --> UserRepository
    AuthService --> JwtService
    ConversationService --> ConversationRepository
    MessageService --> MessageRepository
    MessageService --> ConversationRepository
```

## 5. Ciclul de viață al unei conversații

```mermaid
stateDiagram-v2
    [*] --> WAITING : clientul deschide conversația
    WAITING --> ACTIVE : un agent o preia
    ACTIVE --> CLOSED : agentul sau clientul o închide
    WAITING --> CLOSED : clientul renunță
    CLOSED --> [*]
```

## 6. Secvență — autentificare (JWT)

```mermaid
sequenceDiagram
    actor U as Utilizator
    participant F as React
    participant A as AuthController
    participant S as AuthService
    participant R as UserRepository
    participant J as JwtService
    participant DB as PostgreSQL

    U->>F: introduce username + parolă
    F->>A: POST /api/auth/login
    A->>S: login(request)
    S->>R: findByUsername(username)
    R->>DB: SELECT * FROM users
    DB-->>R: user
    R-->>S: User
    S->>S: verifică parola (BCrypt)
    S->>J: generateToken(user)
    J-->>S: token JWT
    S-->>A: AuthResponse(token, role)
    A-->>F: 200 OK { token, role }
    F->>F: salvează token-ul
    F-->>U: redirecționează spre chat
```

## 7. Secvență — trimiterea unui mesaj în timp real

```mermaid
sequenceDiagram
    actor C as Client
    participant FC as React (client)
    participant WS as WebSocket / STOMP
    participant MS as MessageService
    participant DB as PostgreSQL
    participant FA as React (agent)
    actor AG as Agent

    FC->>WS: CONNECT /ws (header Authorization: Bearer JWT)
    FA->>WS: CONNECT /ws (JWT)
    FC->>WS: SUBSCRIBE /topic/conversations/{id}
    FA->>WS: SUBSCRIBE /topic/conversations/{id}

    C->>FC: scrie mesajul
    FC->>WS: SEND /app/chat.send {conversationId, content}
    WS->>MS: sendMessage(dto, principal)
    MS->>MS: verifică dacă expeditorul face parte din conversație
    MS->>DB: INSERT INTO messages
    DB-->>MS: mesaj salvat
    MS-->>WS: broadcast către /topic/conversations/{id}
    WS-->>FC: MESSAGE
    WS-->>FA: MESSAGE
    FA-->>AG: afișează mesajul nou
```

## 8. Secvență — preluarea unei conversații de către agent

```mermaid
sequenceDiagram
    actor C as Client
    participant FC as React (client)
    participant API as ConversationController
    participant CS as ConversationService
    participant WS as STOMP broker
    participant FA as React (agent)
    actor AG as Agent

    C->>FC: „Am nevoie de ajutor”
    FC->>API: POST /api/conversations {subject}
    API->>CS: create(client, subject)
    CS->>CS: status = WAITING
    CS-->>WS: publică pe /topic/queue
    WS-->>FA: conversație nouă în coadă
    AG->>FA: apasă „Preia”
    FA->>API: POST /api/conversations/{id}/assign
    API->>CS: assign(id, agent)
    CS->>CS: status = ACTIVE, agent = agentul curent
    CS-->>WS: publică pe /topic/conversations/{id}
    WS-->>FC: „Agentul X s-a alăturat”
```
