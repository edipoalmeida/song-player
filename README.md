# Song Player

REST API for music catalog management, playlist lifecycle, playback control, and song recommendations.

Built with **Spring Boot 4**, **Spring Data JPA**, **PostgreSQL**, and **Flyway**.

---

## Features

- **Catalog** — browse songs, artists, albums and genres (public, no auth required)
- **Playlists** — create, update, delete, reorder items, bulk-add songs or all songs from an artist
- **Queue** — generate a playback queue with configurable shuffle strategies (`RANDOM`, `NONE`, `SMART`)
- **Recommendations** — get catalog suggestions compatible with a playlist's content
- **Export** — download a playlist as JSON or M3U
- **Player** — centralized player state with play/pause/stop/next/previous/seek controls and **2× speed mode**

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 17+ |
| Docker & Docker Compose | any recent version |
| Gradle (wrapper included) | — |

---

## Running locally

### Option A — Docker Compose (recommended)

Starts PostgreSQL and the API together.

```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |

Default credentials: **`admin` / `admin`**

---

### Option B — Gradle + external database

1. Copy and configure the environment file:

```bash
cp .env.example .env
# Edit .env and set DB_URL, DB_USERNAME, DB_PASSWORD
```

2. Start only the database:

```bash
docker compose up postgres -d
```

3. Run the application:

```bash
./gradlew bootRun
```

---

## API Documentation

Interactive documentation is available via **Swagger UI** at:

```
http://localhost:8080/swagger-ui.html
```

The raw OpenAPI spec (JSON) is at:

```
http://localhost:8080/api-docs
```

### Authentication

Protected endpoints use **HTTP Basic** authentication.  
Songs (`GET /api/v1/songs/**`) are public. All other endpoints require credentials.

Click **Authorize** in the Swagger UI and enter your credentials.

---

## API Overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/songs` | — | List catalog songs |
| `GET` | `/api/v1/songs/{id}` | — | Get a song |
| `POST` | `/api/v1/playlists` | ✓ | Create a playlist |
| `GET` | `/api/v1/playlists` | ✓ | List playlists |
| `GET` | `/api/v1/playlists/{id}` | ✓ | Get a playlist |
| `PUT` | `/api/v1/playlists/{id}` | ✓ | Update playlist metadata |
| `DELETE` | `/api/v1/playlists/{id}` | ✓ | Delete a playlist |
| `POST` | `/api/v1/playlists/{id}/songs` | ✓ | Add a song |
| `POST` | `/api/v1/playlists/{id}/songs/batch` | ✓ | Add multiple songs |
| `POST` | `/api/v1/playlists/{id}/songs/artist` | ✓ | Add all songs from an artist |
| `DELETE` | `/api/v1/playlists/{id}/items/{itemId}` | ✓ | Remove a song |
| `PUT` | `/api/v1/playlists/{id}/order` | ✓ | Reorder playlist items |
| `GET` | `/api/v1/playlists/{id}/queue` | ✓ | Generate playback queue |
| `GET` | `/api/v1/playlists/{id}/recommendations` | ✓ | Get song recommendations |
| `GET` | `/api/v1/playlists/{id}/export` | ✓ | Export playlist (JSON / M3U) |
| `GET` | `/api/v1/player` | ✓ | Get player state |
| `POST` | `/api/v1/player/playlists/{id}/play` | ✓ | Load and play a playlist |
| `POST` | `/api/v1/player/play` | ✓ | Resume playback at 1× speed |
| `POST` | `/api/v1/player/play-2x` | ✓ | Play at 2× speed (1 real sec = 2 song-secs) |
| `POST` | `/api/v1/player/pause` | ✓ | Pause playback |
| `POST` | `/api/v1/player/stop` | ✓ | Stop playback |
| `POST` | `/api/v1/player/next` | ✓ | Skip to next song |
| `POST` | `/api/v1/player/previous` | ✓ | Go to previous song |
| `POST` | `/api/v1/player/seek` | ✓ | Seek to position (seconds) |

> See `requests.http` for ready-to-run example requests.

---

## Running tests

```bash
./gradlew test
```

Tests use an in-memory H2 database — no external dependencies required.

---

## Project structure

```
src/main/java/com/songplayer/
├── api/                  # HTTP layer (controllers, DTOs, error handling)
│   ├── controller/
│   ├── dto/
│   └── error/
├── application/          # Use cases and domain services
│   ├── export/           # Playlist export strategies
│   └── shuffle/          # Queue shuffle strategies
├── config/               # Spring configuration (Security, OpenAPI)
├── domain/               # Domain model
└── persistence/          # JPA entities and repositories
```
