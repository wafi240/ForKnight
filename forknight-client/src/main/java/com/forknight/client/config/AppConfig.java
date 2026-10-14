package com.forknight.client.config;

public final class AppConfig {
    private AppConfig() {}

    // Set this to TRUE when building the app for your friends!
    public static final boolean IS_PRODUCTION = false;

    // Replace this with your actual Render/Railway/AWS URL when you deploy the server
    public static final String PRODUCTION_URL = "https://your-forkn8-server.up.railway.app";
    
    public static final String SERVER_BASE_URL = IS_PRODUCTION ? PRODUCTION_URL : "http://localhost:8080";
    public static final String API_BASE_URL = SERVER_BASE_URL + "/api";
    public static final int CONNECT_TIMEOUT_SECONDS = 10;
    public static final int REQUEST_TIMEOUT_SECONDS = 30;
}
