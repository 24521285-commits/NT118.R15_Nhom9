package com.datezone.app.api;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class ApiContractTest {
    private Context context;
    private SessionStore session;

    @Before public void isolateSession() {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context = new ContextWrapper(target) {
            @Override public Context getApplicationContext() { return this; }
            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return super.getSharedPreferences(name + "_contract_test", mode);
            }
        };
        session = new SessionStore(context);
        session.clear();
    }

    @Test public void publicProfileAcceptsNullFieldsAndEmptyArrays() throws Exception {
        RemoteProfile profile = new RemoteProfile(new JSONObject(
                "{\"id\":3,\"displayName\":\"An\",\"age\":20,\"gender\":\"male\","
                + "\"bio\":null,\"city\":null,\"distanceKm\":null,\"avatarUrl\":null,\"photos\":[],\"interests\":[]}"));
        assertEquals(3L, profile.id);
        assertEquals("", profile.bio);
        assertEquals("", profile.distance);
        assertEquals(0, profile.photos.length);
        assertTrue(profile.interests.isEmpty());
    }

    @Test public void publicProfileKeepsRemotePhotosAndInterestNames() throws Exception {
        RemoteProfile profile = new RemoteProfile(new JSONObject(
                "{\"id\":8,\"displayName\":\"Chi\",\"age\":24,\"gender\":\"female\","
                + "\"photos\":[{\"id\":1,\"url\":\"http://localhost:3000/uploads/a.jpg\",\"position\":0}],"
                + "\"interests\":[{\"id\":5,\"name\":\"Music\"}]}"));
        assertEquals("http://localhost:3000/uploads/a.jpg", profile.photos[0]);
        assertEquals("Music", profile.interests.get(0));
    }

    @Test public void missingSessionDoesNotSendNetworkRequest() throws Exception {
        try {
            new ApiClient(context).execute("GET", "home/feed", null);
            fail("Missing session must fail");
        } catch (java.io.IOException expected) {
            assertTrue(session.accessToken().isEmpty());
        }
    }

    @Test public void expiredAccessTokenRotatesTokensAndRetriesSwipeWithSameBody() throws Exception {
        session.saveTokens(new JSONObject("{\"accessToken\":\"old-access\",\"refreshToken\":\"old-refresh\"}"));
        try (FakeServer server = new FakeServer(
                new Reply(401, "{\"success\":false,\"message\":\"expired\"}"),
                new Reply(200, "{\"success\":true,\"data\":{\"accessToken\":\"new-access\",\"refreshToken\":\"new-refresh\"}}"),
                new Reply(200, "{\"success\":true,\"data\":{\"matched\":false}}"))) {
            JSONObject body = new JSONObject().put("targetId", 3).put("action", "pass");
            JSONObject result = new ApiClient(context).execute("POST", "home/swipe", body);
            assertFalse(result.getJSONObject("data").getBoolean("matched"));
            List<String> requests = server.requests();
            assertTrue(requests.get(0).contains("POST /api/home/swipe"));
            assertTrue(requests.get(0).contains("Bearer old-access"));
            assertTrue(requests.get(1).contains("POST /api/auth/refresh"));
            assertTrue(requests.get(1).contains("old-refresh"));
            assertFalse(requests.get(1).contains("Authorization:"));
            assertTrue(requests.get(2).contains("Bearer new-access"));
            assertTrue(requests.get(2).endsWith(body.toString()));
            assertEquals("new-refresh", session.refreshToken());
        }
    }

    @Test public void rejectedRefreshClearsSessionAndDoesNotReplaySwipe() throws Exception {
        session.saveTokens(new JSONObject("{\"accessToken\":\"old-access\",\"refreshToken\":\"revoked\"}"));
        try (FakeServer server = new FakeServer(
                new Reply(401, "{\"success\":false,\"message\":\"expired\"}"),
                new Reply(401, "{\"success\":false,\"message\":\"revoked refresh token\"}"))) {
            try {
                new ApiClient(context).execute("POST", "home/swipe", new JSONObject().put("targetId", 3).put("action", "like"));
                fail("Revoked token must fail");
            } catch (java.io.IOException expected) {
                assertEquals("revoked refresh token", expected.getMessage());
            }
            assertEquals(2, server.requests().size());
            assertTrue(session.accessToken().isEmpty());
            assertTrue(session.refreshToken().isEmpty());
        }
    }

    @Test public void validationErrorPreservesSessionAndServerMessage() throws Exception {
        session.saveTokens(new JSONObject("{\"accessToken\":\"access\",\"refreshToken\":\"refresh\"}"));
        try (FakeServer server = new FakeServer(new Reply(400, "{\"success\":false,\"message\":\"Invalid targetId\"}"))) {
            try {
                new ApiClient(context).execute("POST", "home/swipe", new JSONObject());
                fail("Validation failure must propagate");
            } catch (java.io.IOException expected) {
                assertEquals("Invalid targetId", expected.getMessage());
            }
            assertEquals(1, server.requests().size());
            assertEquals("access", session.accessToken());
        }
    }

    private static final class Reply {
        final int status;
        final String body;
        Reply(int status, String body) { this.status = status; this.body = body; }
    }

    /** Runs entirely on the test device; never calls a real backend. */
    private static final class FakeServer implements AutoCloseable {
        final ServerSocket socket;
        final FutureTask<List<String>> task;

        FakeServer(Reply... replies) throws Exception {
            assumeTrue("Use -Pdatezone.apiBaseUrl=http://127.0.0.1:38127/api/ for transport tests",
                    ApiClient.baseUrl().equals("http://127.0.0.1:38127/api/"));
            socket = new ServerSocket(38127);
            socket.setSoTimeout(10000);
            task = new FutureTask<>(() -> {
                List<String> requests = new ArrayList<>();
                for (Reply reply : replies) {
                    try (Socket peer = socket.accept()) {
                        peer.setSoTimeout(10000);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(peer.getInputStream(), StandardCharsets.UTF_8));
                        StringBuilder request = new StringBuilder();
                        int length = 0;
                        String line;
                        while ((line = reader.readLine()) != null && !line.isEmpty()) {
                            request.append(line).append('\n');
                            if (line.toLowerCase(java.util.Locale.ROOT).startsWith("content-length:"))
                                length = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
                        }
                        for (int i = 0; i < length; i++) request.append((char) reader.read());
                        requests.add(request.toString());
                        byte[] payload = reply.body.getBytes(StandardCharsets.UTF_8);
                        String header = "HTTP/1.1 " + reply.status + " Test\r\nContent-Type: application/json\r\nContent-Length: "
                                + payload.length + "\r\nConnection: close\r\n\r\n";
                        peer.getOutputStream().write(header.getBytes(StandardCharsets.UTF_8));
                        peer.getOutputStream().write(payload);
                        peer.getOutputStream().flush();
                    }
                }
                return requests;
            });
            new Thread(task, "contract-http-server").start();
        }

        List<String> requests() throws Exception { return task.get(15, TimeUnit.SECONDS); }
        @Override public void close() throws Exception { socket.close(); }
    }
}
