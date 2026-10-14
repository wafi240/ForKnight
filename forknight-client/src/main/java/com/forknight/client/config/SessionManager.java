package com.forknight.client.config;

import com.forknight.common.dto.auth.LoginResponse;
import com.forknight.common.util.JsonUtil;
import java.util.prefs.Preferences;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private static final String PREF_NODE = "com/forknight/client";
    private static final String PREF_CURRENT_USER = "currentUser";

    private String accessToken;
    private String refreshToken;
    private LoginResponse currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void saveSession(LoginResponse loginResponse) {
        this.currentUser = loginResponse;
        this.accessToken = loginResponse.accessToken();
        this.refreshToken = loginResponse.refreshToken();
        
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(PREF_CURRENT_USER, JsonUtil.toJson(loginResponse));
    }

    public void clearSession() {
        this.currentUser = null;
        this.accessToken = null;
        this.refreshToken = null;
        
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.remove(PREF_CURRENT_USER);
    }

    public boolean isLoggedIn() {
        return accessToken != null && currentUser != null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public LoginResponse getCurrentUser() {
        return currentUser;
    }

    public void loadSavedSession() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String userJson = prefs.get(PREF_CURRENT_USER, null);
        if (userJson != null) {
            try {
                LoginResponse resp = JsonUtil.fromJson(userJson, LoginResponse.class);
                this.currentUser = resp;
                this.accessToken = resp.accessToken();
                this.refreshToken = resp.refreshToken();
            } catch (Exception e) {
                clearSession();
            }
        }
    }
}
