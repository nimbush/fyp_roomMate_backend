# CLAUDE.md — RoomMate Finder (Backend)

> Guidance for Claude Code working on this repository. Read this before making changes.
> **Author:** Nimesh Nepal (NP069711) — APU final-year project.
> **Scope of this file:** the Spring Boot backend (`RoomMate_Filter`). Flutter frontend lives elsewhere.
>
> **Note on the Figma:** The UI reference is the "RoomMate Finder – Orange Mobile UI" Figma file.
> Screens marked *(verify in Figma)* below are inferred from the project proposal and may need
> field/label adjustments once the actual frames are confirmed. When a screen's required fields
> are ambiguous, prefer the fields already present on the `AppUser` model.

---

## 1. Project summary

A mobile-based roommate-matching app for students and young professionals. Users build a
lifestyle/personality profile, get a swipe-based discovery deck of candidate roommates ranked by a
**weighted compatibility algorithm**, mutually "like" to form a match, then chat in real time.
Core academic deliverable = the compatibility scoring engine.

## 2. Tech stack

- Java 17, Spring Boot 3.4.5 (Maven, wrapper committed: use `./mvnw`)
- Spring Web, Spring Security (stateless JWT), Spring Data JPA, Spring WebSocket (STOMP)
- PostgreSQL (persistent data), Redis (OTP storage with TTL), Spring Mail (Gmail SMTP for OTP)
- Lombok, jjwt 0.12.6
- Docker + docker-compose (app + Postgres + Redis)
- Base package: `com.apu.roommate_filter`

## 3. Build, run, test

```bash
# Local run (expects Postgres on 5433 and Redis reachable; see application.properties)
./mvnw spring-boot:run

# Build
./mvnw clean package

# Tests
./mvnw test

# Full stack
docker compose up --build
```

- Server port: `8010` (override `SERVER_PORT`)
- DB defaults: `jdbc:postgresql://localhost:5433/roommate_filter` (override via `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`)
- `spring.jpa.hibernate.ddl-auto=update` (schema auto-managed in dev)
- Images stored on disk at `app.image.folder` (`/app/images` in Docker)

## 4. Architecture & conventions

Standard layered Spring layout — follow the existing pattern exactly:

```
controller/   REST + STOMP entry points
service/      business logic
repository/   Spring Data JPA interfaces
model/        @Entity classes
dto/          request/response objects (authentication/, users/, util/)
mapper/       static toDTO / toEntity helpers
config/       SecurityConfiguration, WebSocketConfig
filter/       JwtFilter
util/         JwtUtil
```

Conventions to keep consistent:
- Lombok everywhere (`@Data`, `@RequiredArgsConstructor`, `@AllArgsConstructor`).
- Constructor injection (final fields), not field injection, in new classes.
- Mappers are static utility classes (see `MatchMapper`, `SwipeMapper`).
- Controllers stay thin; logic goes in services.
- Return `ResponseEntity<?>`; do **not** leak raw `e.getMessage()` to clients (existing code does this — fix when touched, don't copy it).

---

## 5. CURRENT STATE — what is DONE

### Authentication (working)
- `POST /register` → creates unverified user, BCrypt(12) password, sends OTP email.
- `POST /verify` → checks Redis OTP (6-digit, 5-min TTL), sets `verified`, returns `{ token, userId }`.
- `POST /login` → authenticates, returns `{ token, userId }`.
- Stateless security chain; `JwtFilter` validates tokens; public routes: `/login /verify /register /ws-chat/**`.

### Profile (working)
- `GET /api/profile?userId=` → profile + image list + `profileCompleted` flag.
- `PUT /api/profile` (multipart/form-data) → updates all lifestyle fields + images, runs validation
  (budget sanity, ≥4 images when searching roommate, ≥1 image when searching room).
- `AppUser` model holds the full feature set: `minBudget/maxBudget`, `sleepingSchedule`,
  `cleanliness` (int), `smoking`, `diet`, `hobbies`, `musicPreference`, `petPreference`,
  `preferredMale/preferredFemale`, `roommateGender`, `student`, `studyLevel`, `occupation`,
  `searchingRoommate`, `searchingRoom`, `propertyDescription`, `notes`.

### Images (working)
- `POST /upload` (multipart) → stores image to disk, persists `Image` row.

### Match management (partial — see gaps)
- `GET /api/matches/user/{userId}` / `/accepted` / `/pending`
- `PUT /api/matches/accept/{matchId}` / `reject/{matchId}`
- `Match` entity has `matchScore`, status `PENDING/ACCEPTED/REJECTED`, timestamps.

### Real-time chat (partial — broadcast only)
- STOMP `@MessageMapping("/chat.send/{matchId}")` broadcasts to `/topic/chat/{matchId}`.
- `WebSocketConfig` + `/ws-chat` endpoint exist.

### Misc (working)
- `POST /api/support` (help/support), `GET /api/services/{service}` (static NOTIFICATION/PRIVACY content).
- Docker, Compose, `.env` present.

---

## 6. REMAINING WORK — prioritized

> Work top-to-bottom. Items 1–3 are the heart of the project and block a working demo.

### P0 — Compatibility matching engine (THE core deliverable)
Currently `Match.matchScore` is never computed and there is **no algorithm anywhere**.
- [ ] Create `service/CompatibilityService` implementing the weighted score from the report:
      `Compatibility = Σ(wᵢ × sᵢ)` with weights **Personality 40% / Lifestyle 35% / Habits 25%**
      (or cosine similarity over a normalized feature vector — implement the one the written
      report commits to; do not invent a third approach).
- [ ] Build a numeric feature vector from `AppUser` (sleep schedule, cleanliness, smoking, diet,
      pet, music, budget overlap, hobbies overlap, gender preference compatibility).
- [ ] Return a 0–100 percentage score; store it on `Match` at creation.
- [ ] **Write unit tests** for the scorer (the proposal promises "match accuracy" evaluation —
      this is examinable). Cover identical profiles → ~100, opposite profiles → low, gender-pref mismatch → excluded.

### P0 — Swipe → Match pipeline (scaffolding exists, no logic)
`Swipe`, `SwipeDto`, `SwipeMapper`, `SwipesRepository` exist but there is **no controller/service**,
so swipes can't be recorded and matches are never created.
- [ ] `service/SwipeService.recordSwipe(swiperId, swipedId, liked)`:
      persist swipe; if `liked` and a reciprocal like exists → create `ACCEPTED` `Match`
      (compute & store `matchScore`); guard against duplicate swipes (`findBySwiperIdAndSwipedId`).
- [ ] `controller/SwipeController` → `POST /api/swipes`.
- [ ] Decide PENDING semantics: either create a PENDING match on first like (so the other side sees
      it under `/pending`) or only create on mutual like. The existing `getPendingMatchesWhereUserIsUser2`
      query implies the first model — align the service with it.

### P0 — Discovery / feed endpoint (swipe deck — does not exist)
- [ ] `GET /api/discovery?userId=` returns a ranked list of candidate users:
      exclude self, exclude already-swiped (`findBySwiperId`), respect `roommateGender`/
      `preferredMale`/`preferredFemale`, optional budget overlap, only `profileCompleted` users,
      sorted by compatibility score descending. This feeds the swipe screen *(verify deck card fields in Figma)*.

### P1 — Chat persistence (currently in-memory only)
- [ ] `model/ChatMessage` entity (matchId, senderId, content, messageType, sentAt) + repository.
- [ ] Persist messages in `ChatSocketController` before broadcasting.
- [ ] `GET /api/chat/{matchId}/messages` (history) and `GET /api/chat/conversations?userId=` (thread list).
- [ ] *(verify in Figma)* chat list screen + conversation screen field needs.

### P1 — Security hardening (examiner will probe these)
- [ ] **IDOR:** endpoints take `userId`/`matchId` with no ownership check. Resolve the acting user
      from the authenticated principal (JWT) and verify they own the resource. Put `userId` in the JWT
      claims instead of `generatedToken(email, email)`.
- [ ] **WebSocket auth:** `/ws-chat/**` is `permitAll` and the STOMP handler trusts payload
      `senderId`/`matchId`. Authenticate the socket and verify the sender belongs to the match.
- [ ] **Secrets:** `application.properties` hardcodes a live Gmail app password and DB password.
      Move to env vars (`.env` already exists) and **rotate the exposed Gmail password**.
- [ ] Stop returning raw exception messages to clients.

### P2 — Auth completeness
- [ ] Forgot-password / reset flow, resend-OTP, basic rate limiting on OTP + login.
- [ ] Return `userId` from `/register` response.

### P2 — Static content & screens *(verify in Figma)*
- [ ] `ServiceType` only has `NOTIFICATION` and `PRIVACY`. Add screens the Figma defines
      (likely About / Terms / FAQ) and back them via `ServiceContentService`.
- [ ] Confirm settings/onboarding fields map 1:1 to `AppUser`; add any missing field both to the
      model and `ProfileUpdateRequest`.

### P2 — Testing & quality
- [ ] Only the default context-load test exists. Add service-layer tests (compatibility, swipe→match)
      and a couple of controller slice tests.

---

## 7. Guardrails for Claude Code

- **Match existing patterns** (layering, Lombok, static mappers, constructor injection). Don't
  introduce a new style.
- Reuse existing entities/DTOs/repos rather than duplicating them — the swipe/match scaffolding is
  already there.
- Don't change the package name, port, or DB config without being asked.
- Keep `ddl-auto=update` working: new entities must be JPA-clean (no breaking column renames on
  existing tables without a migration note).
- When adding an endpoint, also add: ownership check, validation, and a clean error response.
- Don't commit secrets. If you touch `application.properties`, externalize credentials.
- Before implementing the compatibility math, confirm with the user whether the report commits to
  **weighted sum** or **cosine similarity**, and implement exactly that.
- If a UI requirement is unclear, ask for the specific Figma frame rather than guessing field names.

## 8. Suggested order of attack for a working demo

1. CompatibilityService + unit tests.
2. SwipeService + SwipeController.
3. Discovery endpoint.
4. Wire matchScore into match creation.
5. Chat persistence + history endpoints.
6. Security hardening (IDOR, WS auth, secrets).
