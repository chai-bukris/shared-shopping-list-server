# Shared Shopping List

A shared household shopping list with an Android app and a small Python socket server. The app lets people add missing items as plain text, view the shared list, select items, and remove purchased items.

## Repository layout

- `android-app/` — the Java/XML Android Studio project.
- `server.py`, `requirements.txt` — the Python HTTP server and its dependency list.
- `render.yaml` — Render's configuration for the Python web service. Render builds and starts the server from the repository root; it does not build the Android project.

## Android app

Open the `android-app/` folder in Android Studio. The app requires Android 7.0 (API 24) or newer and an internet connection. Its server address is configured in `android-app/app/src/main/java/com/example/sharedshoppinglist/ApiClient.java`.

To build and install from Android Studio, select a connected Android device and click **Run**. To build a debug APK, use **Build > Build APK(s)**. The APK is generated at:

```text
android-app/app/build/outputs/apk/debug/app-debug.apk
```

The generated APK and machine-specific Android Studio settings are not committed to this repository.

## Server API

- `GET /api/items` — returns the items as UTF-8 text, one item per line.
- `POST /api/items` — adds the raw UTF-8 request body as one item.
- `DELETE /api/items` — removes the submitted comma-separated items.
- `GET /health` — returns `ok` for Render's health check.

The server also accepts the older `ADD` and `REMOVE` methods for compatibility. The Android app uses `POST` and `DELETE`.

## Database and server configuration

The server listens on `0.0.0.0` and uses the `PORT` environment variable provided by Render. When `DATABASE_URL` is set to a PostgreSQL connection string, the server creates the `shopping_items` table if needed and stores items in PostgreSQL. The live database is hosted separately (currently Supabase); database contents and credentials are not stored in GitHub. Without `DATABASE_URL`, the server falls back to temporary in-memory storage, which is lost when the service restarts.

## Run the server locally

Install dependencies and set `DATABASE_URL` to a PostgreSQL connection string if persistent storage is desired, then run:

```sh
pip install -r requirements.txt
python server.py
```

The default local port is `8000`. An Android emulator can reach the host computer at `http://10.0.2.2:8000/api/`. For a physical phone, use the computer's LAN IP address instead.

## Render deployment

Render deploys the Python service from the repository root using `render.yaml`. Set `DATABASE_URL` in the Render service's environment settings; do not commit database credentials. The current Android app URL is `https://shared-shopping-list-server.onrender.com/api/`.

The API currently has no authentication. Anyone who knows the service URL can view and change the shared list, so do not store sensitive information in it.
