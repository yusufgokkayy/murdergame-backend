# 🕵️‍♂️ Murder Game - Interactive Real-Time Backend

This is the backend server for **Murder Game**, a real-time multiplayer team game that combines trivia, betting mechanics, and a murder mystery (Clue Game) module. Built with **Spring Boot** and **WebSockets**, it provides a seamless, zero-refresh experience for game participants.

## 🚀 Key Features

### 1. Real-Time Quiz Engine (WebSockets)
* **Live Broadcasting:** Questions, room states, and correct answers are pushed to clients instantly.
* **Server-Side Timer:** An autonomous 60-second timer (`ScheduledExecutorService`) runs securely on the backend. When time is up, the system automatically evaluates answers and broadcasts results. Double-click safety is implemented.
* **Spokesperson Logic:** Only designated team "Spokespersons" can submit answers, preventing team-internal data conflicts and spam.

### 2. Hybrid Quiz System (Standard & Betting)
* **Quiz 1 (Standard):** Teams earn fixed points for correct answers.
* **Quiz 2 (Betting/Risk):** Teams can wager their existing points (`betAmount`). Correct answers add the wager to their score, while incorrect answers deduct it.

### 3. Murder Mystery (Clue Game)
* A separate asynchronous game mode where teams evaluate clues to guess the "Killer".
* Real-time guess submission (restricted to Spokespersons).
* Admin console for scoring guesses (+50, 0, +100) and triggering the "Final Answer" time.

### 4. Advanced Team Management
* Secure team joining via `teamNo` and `teamPasswordHash`.
* Admin controls for bulk team additions to Game Rooms and assigning Spokespersons.

### 5. Live Leaderboard & Detailed Reporting
* **Dynamic Leaderboard:** Aggregates scores from both Quiz answers and Clue Game guesses in real-time.
* **Results Module:** Advanced filtering that separates Quiz 1 (no bet) and Quiz 2 (bet > 0) statistics, providing detailed post-game reports (corrects, wrongs, total gained/lost) without crashing or data overlapping.

### 6. Robust Security & Validation
* **JWT Authentication:** Stateless security architecture. WebSockets are also secured using a custom `ChannelInterceptor` that parses JWTs on connection.
* **Role-Based Access Control:** Strict separation between `PUBLIC`, `USER`, and `ADMIN` endpoints.
* **Username Validation:** Custom blocklist to prevent reserved keywords and inappropriate usernames.

## 🛠️ Technology Stack
* **Java 17+**
* **Spring Boot 3.x**
* **Spring Security & JWT** (JSON Web Tokens)
* **Spring WebSockets & STOMP**
* **Spring Data JPA & Hibernate**
* **Database:** (e.g., PostgreSQL/MySQL/H2)
* **Lombok**

## 🌐 API Endpoints Reference

### 🔐 Authentication & Users
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/auth/admin/login` | Admin login to get JWT | Public |
| `POST` | `/api/auth/user/login` | Team/User login | Public |
| `POST` | `/api/auth/user/register` | Register a new user | Public |
| `GET`  | `/api/auth/users` | List all registered users | Public |
| `DELETE`| `/api/admin/users/{userId}` | Delete a user | Admin |

### 👥 Team Management
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET`  | `/api/team/all` | Get list of all teams | Public |
| `POST` | `/api/team/join` | User joins a team | User |
| `POST` | `/api/team/admin/create` | Create a new team | Admin |
| `POST` | `/api/team/admin/{teamId}/set-spokesperson/{userId}` | Assign team spokesperson | Admin |
| `POST` | `/api/team/admin/add-user/{teamId}` | Force add user to team | Admin |
| `POST` | `/api/team/admin/remove-user/{userId}` | Remove user from team | Admin |

### 🎮 Game Room & Quiz
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/game-room/create` | Create a new game room | Admin |
| `PUT`  | `/api/game-room/{roomId}/state` | Update game state (e.g., QUIZ1) | Admin |
| `POST` | `/api/game-room/{roomId}/add-teams` | Bulk add teams to a room | Admin |
| `GET`  | `/api/quiz/room/{gameRoomId}/questions` | Get all questions for a room | Public |
| `POST` | `/api/quiz/room/{gameRoomId}/questions/multiple`| Bulk insert questions | Admin |
| `GET`  | `/api/quiz/room/{gameRoomId}/answers` | Get all submitted answers | Admin |

### 🕵️ Clue Game (Murder Mystery)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `POST` | `/api/admin/clue-game/start/{clueGameId}` | Trigger game start event | Admin |
| `POST` | `/api/admin/clue-game/final-time/{clueGameId}`| Trigger final guess phase | Admin |
| `POST` | `/api/admin/clue-game/guess/{guessId}/score`| Score a submitted guess | Admin |
| `POST` | `/api/admin/clue-game/end/{clueGameId}` | End the clue game | Admin |

### 🏆 Leaderboard & Results
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET`  | `/api/leaderboard` | Get current total scores | Auth |
| `GET`  | `/api/results/all` | Get full detailed final results | Auth |
| `GET`  | `/api/results/quiz1/{teamId}` | Get filtered Quiz 1 stats | Auth |
| `GET`  | `/api/results/quiz2/{teamId}` | Get filtered Quiz 2 (Bet) stats | Auth |

---

## 📡 WebSocket Channels Overview (STOMP)

Connection Endpoint: `/ws` (Secured via JWT Channel Interceptor)

### 📥 Subscriptions (Listen to these channels)
* **Quiz & Game Room:**
    * `/topic/room/{roomId}/state` - Triggered when game phase changes.
    * `/topic/room/{roomId}/question` - Pushes question details & starts 60s timer.
    * `/topic/room/{roomId}/question-result` - Pushes correct answer when time is up.
    * `/topic/room/{roomId}/leaderboard` - Live updates of scores.
* **Clue Game:**
    * `/topic/clue-game/start` / `/final-time` / `/end` - Phase transitions.
    * `/topic/clue-game/{teamId}/guesses` - Echoes team guesses.
    * `/topic/clue-game/{teamId}/score` - Pushes admin score decisions.

### 📤 Destinations (Send messages here)
* **Quiz:**
    * `/app/quiz/{roomId}/answer` - Submit answer and bet amount (Spokesperson only).
    * `/app/quiz/{roomId}/next-question` - Start next question (Admin only).
* **Clue Game:**
    * `/app/clue-guess` - Submit a killer guess (Spokesperson only).
    * `/app/final-answer` - Submit the final narrative answer (Spokesperson only).

**Client Subscriptions (Listening):**
* `/topic/room/{roomId}/state` -> Game room state changes.
* `/topic/room/{roomId}/question` -> Incoming questions.
* `/topic/room/{roomId}/question-result` -> Correct answers when time is up.
* `/topic/room/{roomId}/leaderboard` -> Real-time scoreboard updates.
* `/topic/clue-game/...` -> Clue game events (start, final-time, score updates).

**Client Destinations (Sending):**
* `/app/quiz/{roomId}/answer` -> Submit quiz answer.
* `/app/clue-guess` -> Submit clue game guess.

## ⚙️ Core Modules
* `auth`: Login, registration, and JWT generation.
* `game`: Game Room (Session) state management.
* `quiz`: Question management and WebSocket Quiz Controller.
* `cluegame`: Murder mystery entity and event handlers.
* `team`: Team creation and Spokesperson assignment.
* `results`: Complex data aggregation for end-of-game statistics.