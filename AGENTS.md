# AGENTS Guide

## Project Snapshot
- Spring Boot 4 + Java 25 service that receives listening history and persists normalized music catalog data (`pom.xml`, `src/main/resources/application.yaml`).
- Main flow is **ingest -> Spotify enrich -> upsert-like DAO writes -> history insert** in `SongHistoryRegistrationService.register`.
- Persistence is JDBC-first (manual SQL with `NamedParameterJdbcTemplate`), not Spring Data repositories (`src/main/java/dev/atilioaraujo/music/datasetapi/dao/*Dao.java`).

## Runtime/Data Flow You Need To Know
- HTTP entrypoint: `POST /api/song/history` in `SongHistoryController`.
- Request payload is a single object `SongHistoryRegistrationRequest` with JSON keys `musica`, `artista`, and either `timestamp` (epoch seconds) or `data_hora` (`Instant`).
- `SongHistoryRegistrationService` resolves `playedAt` in UTC, blocks duplicates by exact datetime+song via `SongHistoryDao.findByPlayedDateAndSongNameIgnoreCase`, then enriches catalog metadata using Spotify.
- Catalog resolution order is Artist -> Album -> Song, creating missing rows before writing `song_history`.
- `SongHistoryAlreadyRegisteredException` is mapped to HTTP **201** (idempotent-ish behavior) in `SongHistoryControllerAdvice`; invalid payload maps to **400** with `ApiErrorResponse`.

## Integration Boundaries
- Spotify integration uses `spotify-web-api-java` via a shared `SpotifyApi` bean (`SpotifyConfiguration`).
- Auth tokens are cached in-memory in `SpotifyAuthenticationService` (`cachedToken`) with refresh skew from `spotify.token-refresh-skew-seconds`.
- App authenticates at startup (`StartupActions` `CommandLineRunner`), so missing Spotify credentials can break full app startup.
- DB schema reference: `database/script.sql` (`artist`, `album`, `song`, `song_history` tables and FK chain).

## Local Dev Workflows (Observed)
- Build/test uses Maven (`pom.xml`); Maven wrapper files are not present in repo.
- Typical commands from project root:
  - `mvn clean test`
  - `mvn -Dtest=SongHistoryRegistrationServiceTest test`
  - `mvn spring-boot:run`
- Required env vars for real runs: `DB_USERNAME`, `DB_PASSWORD`, `SPOTIFY_CLIENT_ID`, `SPOTIFY_CLIENT_SECRET` (`application.yaml`).
- SQL debug logging is enabled for JDBC templates by default in `application.yaml`.

## Code Patterns/Conventions Specific To This Repo
- Domain models are Java `record`s with Spring Data relational annotations (see `domain/*.java`), but persistence behavior is implemented manually in DAOs.
- DAO methods return inserted entities with generated IDs using `GeneratedKeyHolder`.
- Case-insensitive matching is done at SQL level with `LOWER(...) = LOWER(:name)`.
- Song uniqueness in service logic is contextual (song name + album), not enforced by DB unique constraints.
- Service methods validate inputs explicitly and throw `IllegalArgumentException`/domain exceptions instead of Bean Validation annotations.

## Known Mismatches To Verify Before Refactors
- Tests in `SongHistoryControllerTest` currently post to `/song/history` and send array payloads, while controller expects `/api/song/history` and a single object.
- The `SongDao.insert` return object uses `song.nameStreaming()` for both source and streaming names; preserve or fix intentionally with matching test updates.

