package com.datezone.app.api;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

/** Shared contract with the login flow. Pass response.data.tokens to saveTokens(). */
public final class SessionStore {
    private final SharedPreferences prefs;

    public SessionStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("datezone_session", Context.MODE_PRIVATE);
    }

    public String accessToken() { return prefs.getString("accessToken", ""); }
    public String refreshToken() { return prefs.getString("refreshToken", ""); }

    public void saveTokens(JSONObject tokens) throws JSONException {
        String access = tokens.getString("accessToken");
        String refresh = tokens.getString("refreshToken");
        if (access.isEmpty() || refresh.isEmpty()) throw new JSONException("Thiếu token đăng nhập");
        prefs.edit().putString("accessToken", access).putString("refreshToken", refresh).apply();
    }

    public void clear() { prefs.edit().clear().apply(); }
}
