# Shared Shopping List server

A small HTTP server implemented with Python's standard-library sockets. If `DATABASE_URL` is set, it stores items in PostgreSQL using Psycopg 3. Without that variable, it uses temporary in-memory storage so the web service can run by itself.

## API

- `GET /api/items` — returns one item per line as UTF-8 plain text.
- `ADD /api/items` — adds the raw UTF-8 request body as one item.
- `REMOVE /api/items` — removes the submitted comma-separated items.
- `GET /health` — returns `ok` for Render's health check.

The server listens on `0.0.0.0` and uses the `PORT` environment variable supplied by Render. When `DATABASE_URL` points to a PostgreSQL database, it creates the `shopping_items` table on startup.

## Run locally

To use persistent storage locally, set `DATABASE_URL` to a PostgreSQL connection string before starting the server. Without it, the list is kept in memory only.

```sh
python server.py
```

The default local port is `8000`. The Android app's API base URL should then be `http://10.0.2.2:8000/api/` when using the Android emulator. For a physical phone, use the computer's LAN IP address instead of `10.0.2.2`.

## Deploy to Render

Create a **Web Service** from this repository's `main` branch and choose the Free plan. The included `render.yaml` now defines only the Python web service; it does not create a Render database. Render provides an HTTPS URL after deployment.

Set `BASE_URL` in the Android app's `ApiClient.java` to that URL with `/api/` at the end, for example:

```java
private static final String BASE_URL = "https://shared-shopping-list-api.onrender.com/api/";
```

The Render URL is assigned by Render and may differ from the example.

## Persistent storage option

For persistent storage, create a PostgreSQL project with a provider such as Supabase, copy its connection string, and add it to the Render web service as the `DATABASE_URL` environment variable. On restart, the server automatically creates its table if it does not exist.

Supabase's Free projects can pause after a week of low activity. A paused project can be resumed with its data for up to one year. The Free plan currently includes 500 MB per database and no automatic backups. See Supabase's current [free-plan details](https://supabase.com/pricing) and [pause behavior](https://supabase.com/docs/guides/platform/free-project-pausing).

Without `DATABASE_URL`, Render's in-memory list is lost when the free web service spins down or restarts.

The endpoint currently has no authentication, so anyone who knows the service URL can view and change the list. Do not use it for private data.
