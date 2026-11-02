# Support Chat

Aplicație web de chat în care utilizatorii discută în timp real cu agenți de suport.

## Stack
- **Backend:** Spring Boot 4.1 (Java 21) — Web, WebSocket, Data JPA, Security, Validation, Lombok
- **Frontend:** React + Vite — axios, @stomp/stompjs, react-router-dom
- **Bază de date:** PostgreSQL 17 (Docker)

## Structură
```
backend/            Spring Boot (port 8080)
frontend/           React + Vite (port 5173, proxy /api și /ws către backend)
docker-compose.yml  PostgreSQL
```

## Pornire
```bash
docker compose up -d          # baza de date
cd backend && ./mvnw spring-boot:run
cd frontend && npm run dev     # http://localhost:5173
```

## Documentație
Diagramele (arhitectură, cazuri de utilizare, ER, clase, stări, secvențe) sunt în
[docs/diagrame.md](docs/diagrame.md), iar exportul PNG în [docs/img/](docs/img/).
