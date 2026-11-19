# VenderLive (Android)

Native Android (Kotlin + Jetpack Compose) audio room app. Phase 1 covers the
login flow and the app shell. All third-party services are our own accounts:
our Firebase project, our Agora app, our backend on our own VPS. No credentials
or code from any other app are used.

## Current scope (phase 1)

1. Phone + OTP login (Firebase Auth proves phone ownership)
2. Server-side session: Firebase ID token is exchanged for our own access token
   at `POST /v1/auth/firebase-login`
3. Home shell: room list placeholder, bottom navigation, logout
4. FCM receiver placeholder
5. Manual GitHub Actions build (`workflow_dispatch` only, no auto build yet)

## Project layout

```
app/src/main/java/com/exbottod/venderlive/
  App.kt                  manual dependency wiring
  MainActivity.kt         Compose host
  data/model              API DTOs
  data/remote             Retrofit client + API contract
  data/local              DataStore session storage
  data/repository         AuthRepository (Firebase -> our backend)
  ui/login                LoginScreen + LoginViewModel (phone step, OTP step)
  ui/home                 Home shell
  ui/theme                Colors and typography
  navigation              NavHost
  push                    FCM service placeholder
```

## Setup

1. Copy `local.properties.example` to `local.properties` and fill
   `API_BASE_URL`, `WS_URL`, `AGORA_APP_ID`.
2. Copy `app/google-services.json.example` to `app/google-services.json` and
   replace it with the real file from our Firebase project
   (package name must be `com.venderlive.app`).
3. Firebase console: enable Phone sign-in, add SHA-1 + SHA-256 of the signing
   key (needed for auto-verification).
4. Backend must expose `POST /v1/auth/firebase-login` and `GET /v1/me`.

## Backend contract (phase 1)

```
POST /v1/auth/firebase-login
{ "id_token": "<firebase id token>" }

200 OK
{
  "code": 0,
  "data": {
    "access_token": "<jwt from our server>",
    "refresh_token": "<optional>",
    "user": { "id": "u_1", "nickname": "new_user", "level": 0, "is_new": true }
  }
}
```

The server verifies the Firebase ID token with the Firebase Admin SDK, creates
or loads the user, and issues its own session token. Firebase is never the
session authority.

## Phase 2 (next, not built yet)

1. `GET /v1/rooms` room list, `GET /v1/rooms/{id}` room detail with seat map
2. Seat model: `index, userId, muted, locked, role`
3. Seat actions: join, leave, request queue, approve/reject (host/admin),
   mute, kick, lock
4. Agora RTC join with `uid = seat index`, token minted by our backend
5. Room events over our WebSocket (`WS_URL`), not a third-party IM SDK
