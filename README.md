# Shared Shopping List server

A small HTTP server implemented with Python's standard-library sockets. It stores items in Render Postgres using Psycopg 3.

## API

- `GET /api/items` — returns one item per line as UTF-8 plain text.
- `ADD /api/items` — adds the raw UTF-8 request body as one item.
- `REMOVE /api/items` — removes the submitted comma-separated items.
- `GET /health` — returns `ok` for Render's health check.

The server listens on `0.0.0.0` and uses the `PORT` environment variable supplied by Render. It reads the database connection string from `DATABASE_URL` and creates the `shopping_items` table on startup.

## Run locally

Set `DATABASE_URL` to a PostgreSQL connection string before starting the server.

```sh
python server.py
```

The default local port is `8000`. The Android app's API base URL should then be `http://10.0.2.2:8000/api/` when using the Android emulator. For a physical phone, use the computer's LAN IP address instead of `10.0.2.2`.

## Deploy to Render

Create a **Blueprint** from this repository's `main` branch. The included `render.yaml` creates both the free web service and a free Render Postgres database, then connects them through `DATABASE_URL`. After deployment, Render provides an HTTPS URL such as `https://shared-shopping-list-api.onrender.com`.

Set `BASE_URL` in the Android app's `ApiClient.java` to that URL with `/api/` at the end, for example:

```java
private static final String BASE_URL = "https://shared-shopping-list-api.onrender.com/api/";
```

The Render URL is assigned by Render and may differ from the example.

## Free-plan limits

Render's free Postgres database expires 30 days after creation. After expiry it becomes inaccessible, and Render deletes it after a further 14-day grace period unless it is upgraded. The free database has 1 GB of storage and no backups. See Render's current free-plan limits before using it for anything important.

The endpoint currently has no authentication, so anyone who knows the service URL can view and change the list. Do not use it for private data.
