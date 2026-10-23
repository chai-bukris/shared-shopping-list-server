"""Small shared shopping list HTTP service built directly on Python sockets."""

import os
import socketserver
import threading
from urllib.parse import urlsplit

import psycopg


MAX_BODY_BYTES = 64 * 1024
memory_items = []
memory_items_lock = threading.Lock()


class ShoppingListHandler(socketserver.StreamRequestHandler):
    def handle(self):
        try:
            request_line = self.rfile.readline(8192)
            if not request_line:
                return
            try:
                method, target, _version = request_line.decode("ascii").strip().split(" ", 2)
            except (UnicodeDecodeError, ValueError):
                self.respond(400, "Bad request")
                return

            headers = {}
            while True:
                line = self.rfile.readline(8192)
                if line in (b"\r\n", b"\n", b""):
                    break
                try:
                    name, value = line.decode("iso-8859-1").split(":", 1)
                except ValueError:
                    self.respond(400, "Bad header")
                    return
                headers[name.strip().lower()] = value.strip()

            try:
                length = int(headers.get("content-length", "0"))
            except ValueError:
                self.respond(400, "Invalid Content-Length")
                return
            if length < 0 or length > MAX_BODY_BYTES:
                self.respond(413, "Request body too large")
                return
            body = self.rfile.read(length) if length else b""
            path = urlsplit(target).path.rstrip("/") or "/"

            if method == "GET" and path == "/health":
                self.respond(200, "ok")
            elif path == "/api/items" and method == "GET":
                url = database_url()
                if url:
                    with psycopg.connect(url) as connection:
                        rows = connection.execute(
                            "SELECT item FROM shopping_items ORDER BY id"
                        ).fetchall()
                    found_items = [row[0] for row in rows]
                else:
                    with memory_items_lock:
                        found_items = list(memory_items)
                response = "\n".join(found_items)
                self.respond(200, response)
            elif path == "/api/items" and method in ("ADD", "POST"):
                try:
                    item = body.decode("utf-8")
                except UnicodeDecodeError:
                    self.respond(400, "Body must be UTF-8")
                    return
                if not item.strip():
                    self.respond(400, "Item cannot be empty")
                    return
                url = database_url()
                if url:
                    with psycopg.connect(url) as connection:
                        connection.execute(
                            "INSERT INTO shopping_items (item) VALUES (%s)", (item,)
                        )
                else:
                    with memory_items_lock:
                        memory_items.append(item)
                self.respond(201, "added")
            elif path == "/api/items" and method in ("REMOVE", "DELETE"):
                try:
                    requested = body.decode("utf-8").split(",")
                except UnicodeDecodeError:
                    self.respond(400, "Body must be UTF-8")
                    return
                url = database_url()
                if url:
                    with psycopg.connect(url) as connection:
                        for item in requested:
                            connection.execute(
                                "DELETE FROM shopping_items WHERE id = "
                                "(SELECT id FROM shopping_items WHERE item = %s "
                                "ORDER BY id LIMIT 1)",
                                (item,),
                            )
                else:
                    with memory_items_lock:
                        for item in requested:
                            if item in memory_items:
                                memory_items.remove(item)
                self.respond(200, "removed")
            else:
                self.respond(404 if path not in ("/api/items", "/health") else 405,
                             "Not found" if path not in ("/api/items", "/health") else "Method not allowed")
        except psycopg.Error as error:
            print(f"Postgres request failed: {error}", flush=True)
            self.respond(503, "Database unavailable")
        except (ConnectionError, OSError):
            # Clients may close a connection while the server is responding.
            return

    def respond(self, status, message):
        phrases = {200: "OK", 201: "Created", 400: "Bad Request", 404: "Not Found",
                   405: "Method Not Allowed", 413: "Payload Too Large",
                   503: "Service Unavailable"}
        payload = message.encode("utf-8")
        response = (
            f"HTTP/1.1 {status} {phrases.get(status, 'Error')}\r\n"
            "Content-Type: text/plain; charset=utf-8\r\n"
            f"Content-Length: {len(payload)}\r\n"
            "Connection: close\r\n"
            "Access-Control-Allow-Origin: *\r\n"
            "\r\n"
        ).encode("ascii") + payload
        self.wfile.write(response)


class ThreadingHTTPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    allow_reuse_address = True
    daemon_threads = True


def main():
    initialize_database()
    port = int(os.environ.get("PORT", "8000"))
    server = ThreadingHTTPServer(("0.0.0.0", port), ShoppingListHandler)
    print(f"Shopping list server listening on 0.0.0.0:{port}", flush=True)
    server.serve_forever()


def database_url():
    return os.environ.get("DATABASE_URL")


def initialize_database():
    url = database_url()
    if not url:
        print("DATABASE_URL is not set; using temporary in-memory storage", flush=True)
        return
    with psycopg.connect(url, connect_timeout=15) as connection:
        connection.execute(
            "CREATE TABLE IF NOT EXISTS shopping_items ("
            "id BIGSERIAL PRIMARY KEY, item TEXT NOT NULL)"
        )
    print("Shopping list database is ready", flush=True)


if __name__ == "__main__":
    main()
