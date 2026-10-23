package com.example.sharedshoppinglist;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** All server details live here so they can be adjusted when the backend is ready. */
public final class ApiClient {
    private static final String BASE_URL = "https://shared-shopping-list-server.onrender.com/api/";
    private static final MediaType PLAIN_TEXT = MediaType.get("text/plain; charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            // Render's free instance can need around a minute to wake after inactivity.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(150, TimeUnit.SECONDS)
            .build();

    private ApiClient() { }

    public static String getList() throws IOException {
        return request("GET", "items", null);
    }

    public static void addItem(String rawItem) throws IOException {
        request("POST", "items", rawItem);
    }

    public static void removeItems(String commaSeparatedItems) throws IOException {
        request("DELETE", "items", commaSeparatedItems);
    }

    private static String request(String method, String path, String body) throws IOException {
        RequestBody requestBody = body == null ? null : RequestBody.create(body, PLAIN_TEXT);
        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .method(method, requestBody)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() == null ? "" : response.body().string().trim();
                String detail = errorBody.isEmpty() ? "" : ": " + errorBody;
                throw new IOException("HTTP " + response.code() + detail);
            }
            return response.body() == null ? "" : response.body().string();
        }
    }
}
