# Shared Shopping List server

A small HTTP server implemented with Python's standard-library sockets. It uses no web framework and has no external Python dependencies.

## API

- `GET /api/items` — returns one item per line as UTF-8 plain text.
- `ADD /api/items` — adds the raw UTF-8 request body as one item.
- `REMOVE /api/items` — removes the submitted comma-separated items.
- `GET /health` — returns `ok` for Render's health check.

The server listens on `0.0.0.0` and uses the `PORT` environment variable supplied by Render.

## Run locally

```sh
python server.py
```

The default local port is `8000`. The Android app's API base URL should then be `http://10.0.2.2:8000/api/` when using the Android emulator. For a physical phone, use the computer's LAN IP address instead of `10.0.2.2`.

## Deploy to Render

Create a **Web Service** from this repository and choose the Free plan. The included `render.yaml` sets the build and start commands. After deployment, Render provides an HTTPS URL such as `https://shared-shopping-list-api.onrender.com`.

Set `BASE_URL` in the Android app's `ApiClient.java` to that URL with `/api/` at the end, for example:

```java
private static final String BASE_URL = "https://shared-shopping-list-api.onrender.com/api/";
```

The Render URL is assigned by Render and may differ from the example.

## Prototype limits

The list is held in memory. It is shared while this server process is running, but it is cleared when the service restarts or redeploys. The endpoint currently has no authentication, so anyone who knows the URL can view and change the list. Do not use it for private data.
