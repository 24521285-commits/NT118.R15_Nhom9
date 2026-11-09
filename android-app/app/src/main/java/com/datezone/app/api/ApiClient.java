package com.datezone.app.api;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.datezone.app.BuildConfig;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** JSON transport. Requests and token refresh run off the main thread. */
public final class ApiClient {
    public interface Callback {
        void onSuccess(JSONObject response) throws Exception;
        void onError(String message);
    }
    private static final ExecutorService IO = Executors.newFixedThreadPool(3);
    private static final Object SESSION_LOCK = new Object();
    private final SessionStore session;
    private final Handler main = new Handler(Looper.getMainLooper());

    public ApiClient(Context context) { session = new SessionStore(context); }

    public static String baseUrl() {
        String url = BuildConfig.API_BASE_URL;
        return url.endsWith("/") ? url : url + "/";
    }

    public void request(Activity owner, String method, String path, JSONObject body, Callback callback) {
        IO.execute(() -> {
            try {
                JSONObject result = execute(method, path, body);
                main.post(() -> {
                    if (owner.isFinishing() || owner.isDestroyed()) return;
                    try { callback.onSuccess(result); }
                    catch (Exception e) { callback.onError("Dữ liệu máy chủ không đúng định dạng."); }
                });
            } catch (Exception e) {
                String message = e instanceof java.net.SocketTimeoutException
                        ? "Máy chủ phản hồi quá chậm. Vui lòng thử lại."
                        : e instanceof java.net.ConnectException || e instanceof java.net.UnknownHostException
                        ? "Không kết nối được máy chủ. Vui lòng kiểm tra địa chỉ API và mạng."
                        : e.getMessage();
                main.post(() -> {
                    if (!owner.isFinishing() && !owner.isDestroyed())
                        callback.onError(message == null ? "Không thể tải dữ liệu. Vui lòng thử lại." : message);
                });
            }
        });
    }

    JSONObject execute(String method, String path, JSONObject body) throws Exception {
        String token = session.accessToken();
        if (token.isEmpty()) throw new IOException("Vui lòng đăng nhập để tải dữ liệu từ máy chủ.");
        Reply reply = exchange(method, path, body, token);
        if (reply.status == 401) {
            synchronized (SESSION_LOCK) {
                if (token.equals(session.accessToken())) {
                    String refresh = session.refreshToken();
                    if (refresh.isEmpty()) {
                        session.clear();
                        throw new IOException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                    }
                    Reply renewed = exchange("POST", "auth/refresh",
                            new JSONObject().put("refreshToken", refresh), "");
                    if (renewed.status == 401 || renewed.status == 403) session.clear();
                    requireSuccess(renewed);
                    session.saveTokens(renewed.json.getJSONObject("data"));
                }
                token = session.accessToken();
            }
            if (token.isEmpty()) throw new IOException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            reply = exchange(method, path, body, token);
        }
        requireSuccess(reply);
        return reply.json;
    }

    private static void requireSuccess(Reply reply) throws IOException {
        if (reply.status < 200 || reply.status >= 300 || !reply.json.optBoolean("success"))
            throw new IOException(reply.json.optString("message", "Máy chủ trả về lỗi " + reply.status));
    }

    private static Reply exchange(String method, String path, JSONObject body, String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(baseUrl() + path).openConnection();
        try {
            conn.setRequestMethod(method);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Accept", "application/json");
            if (!token.isEmpty()) conn.setRequestProperty("Authorization", "Bearer " + token);
            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (java.io.OutputStream out = conn.getOutputStream()) {
                    out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = conn.getResponseCode();
            InputStream stream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (stream == null) throw new IOException("Máy chủ không trả về dữ liệu (" + status + ").");
            try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                return new Reply(status, new JSONObject(out.toString("UTF-8")));
            }
        } finally { conn.disconnect(); }
    }

    private static final class Reply {
        final int status;
        final JSONObject json;
        Reply(int status, JSONObject json) { this.status = status; this.json = json; }
    }
}
