# Support Chat — Securitate

## Decizii

| Decizie | Alegere | Motiv |
|---|---|---|
| Autentificare | **JWT** (`Authorization: Bearer <token>`) | frontend React (SPA), API stateless |
| Sesiune pe server | nu (`STATELESS`) | identitatea stă în token, orice instanță îl poate verifica |
| CSRF | dezactivat | token-ul nu pleacă automat de la browser (nu e cookie), deci un site străin nu îl poate folosi |
| Parole | BCrypt (cost 10) | hash lent, cu salt aleator; parola în clar nu ajunge în baza de date |
| Semnătura token-ului | HMAC-SHA256, cheie din `JWT_SECRET` (minim 32 de octeți) | cheia o știe doar serverul |
| Valabilitate token | 1 oră (`app.jwt.validity`) | un token furat rămâne valid cel mult o oră |
| Format erori | ProblemDetail (`application/problem+json`) | același format pentru 400/401/403/404/409 |

### Conținutul token-ului

```json
{
  "iss": "support-chat",
  "sub": "agent",
  "uid": 1,
  "role": "AGENT",
  "iat": 1794211200,
  "exp": 1794214800
}
```

Payload-ul este doar **codat** (Base64URL), nu criptat: nu conține parole sau date personale (email).
Rolul este copiat în token la login, deci o schimbare de rol se aplică după un login nou.

## Roluri

| Rol | Cine | Cum se creează |
|---|---|---|
| `USER` | clientul care cere ajutor | se înregistrează singur (`POST /api/auth/register`) |
| `AGENT` | agentul de suport | migrare Flyway / creat de admin |
| `ADMIN` | administratorul | migrare Flyway |

Înregistrarea creează **doar** conturi `USER`; un agent sau un admin nu se poate crea prin API-ul public.

## Resursă × rol

✅ permis · ❌ interzis · 🔸 permis doar pentru conversațiile proprii (verificat în service)

### REST

| Endpoint | Anonim | USER | AGENT | ADMIN |
|---|:-:|:-:|:-:|:-:|
| `POST /api/auth/register` | ✅ | ✅ | ✅ | ✅ |
| `POST /api/auth/login` | ✅ | ✅ | ✅ | ✅ |
| `GET /api/auth/me` | ❌ 401 | ✅ | ✅ | ✅ |
| `POST /api/conversations` (deschide conversație) | ❌ 401 | ✅ | ❌ 403 | ❌ 403 |
| `GET /api/conversations` (conversațiile mele) | ❌ 401 | 🔸 deschise de el | 🔸 preluate de el | ✅ toate |
| `GET /api/conversations/queue` | ❌ 401 | ❌ 403 | ✅ | ✅ |
| `GET /api/conversations/{id}` | ❌ 401 | 🔸 | 🔸 + cele din coadă | ✅ |
| `POST /api/conversations/{id}/assign` | ❌ 401 | ❌ 403 | ✅ dacă e în coadă (altfel 409) | ❌ 403 |
| `POST /api/conversations/{id}/close` | ❌ 401 | 🔸 | 🔸 | ✅ |
| `GET /api/conversations/{id}/messages` | ❌ 401 | 🔸 | 🔸 | ✅ (doar citire) |
| `POST /api/conversations/{id}/messages` | ❌ 401 | 🔸 | 🔸 | ❌ 403 (nu scrie în conversațiile altora) |
| `POST /api/conversations/{id}/read` | ❌ 401 | 🔸 | 🔸 | ❌ 403 |
| Gestionare utilizatori / agenți *(planificat)* | ❌ 401 | ❌ 403 | ❌ 403 | ✅ |

### WebSocket / STOMP *(planificat)*

| Acțiune | Anonim | USER | AGENT | ADMIN |
|---|:-:|:-:|:-:|:-:|
| `CONNECT` la `/ws` | ❌ (token obligatoriu în header-ul STOMP) | ✅ | ✅ | ✅ |
| `SUBSCRIBE /topic/conversations/{id}` | ❌ | 🔸 | 🔸 | ✅ |
| `SUBSCRIBE` la coada de așteptare | ❌ | ❌ | ✅ | ✅ |
| `SEND` mesaj în conversație | ❌ | 🔸 | 🔸 | ❌ |

Browserul nu poate trimite header-ul `Authorization` la handshake-ul WebSocket, deci token-ul se trimite
în header-ele frame-ului STOMP `CONNECT` și este validat de un `ChannelInterceptor`.
Verificarea la `SUBSCRIBE` este obligatorie: altfel oricine s-ar putea abona la mesajele altei conversații.

## Unde se face fiecare verificare

Trei niveluri, de la general la specific:

1. **Filtre / reguli pe URL** — `SecurityConfig`: token valid (401) și rolul potrivit pentru endpoint (403).
2. **Metodă** — `@PreAuthorize` pe service-uri: rolul este verificat din nou, indiferent dacă metoda
   e apelată din REST sau, mai târziu, din WebSocket.
3. **Date** — în service (`checkAccess`, `hasParticipant`): utilizatorul face parte din conversație.
   Regulile pe URL nu pot verifica asta, pentru că depind de rândul din baza de date.

## Coduri de răspuns

| Cod | Când | `title` |
|---|---|---|
| 400 | date invalide (`@Valid`); `errors: {câmp: mesaj}` | Date invalide |
| 401 | lipsește token-ul, e expirat, modificat sau semnat cu altă cheie | Neautentificat |
| 401 | login cu username inexistent sau parolă greșită (același mesaj) | Autentificare esuata |
| 403 | rolul nu are voie / nu faceți parte din conversație | Acces interzis |
| 404 | conversația nu există | Resursa inexistenta |
| 409 | conversație deja preluată / închisă, username sau email deja folosit | Conflict |

## Conturi de test

| Username | Parolă | Rol |
|---|---|---|
| `agent` | `agent123` | AGENT |
| `admin` | `admin123` | ADMIN |
| *(înregistrare)* | *(aleasă)* | USER |

Parolele de test sunt doar pentru dezvoltare locală; în baza de date există doar hash-urile BCrypt.
