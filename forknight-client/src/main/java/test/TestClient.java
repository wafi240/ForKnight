package test;

import com.forknight.client.api.RepoApi;
import com.forknight.client.config.SessionManager;
import com.forknight.common.dto.auth.LoginResponse;

public class TestClient {
    public static void main(String[] args) {
        try {
            LoginResponse lr = new LoginResponse(1L, "wafi1314", "wafi@example.com", "Wafi", null, "USER", 
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ3YWZpMTMxNCIsInVzZXJJZCI6MSwicm9sZSI6IlVTRVIiLCJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNzkwMzM3MDM2LCJleHAiOjE3OTAzNDA2MzZ9.cfTPRLTCfuvJ9q-dxeXi5lcXoKUwa1XIe7v0t75T117upUZHSkcNYqZeqjRSf_e_B6Z0LzhVZxVjgqcsqUE3Fg", 
                "dummy");
            SessionManager.getInstance().saveSession(lr);

            RepoApi api = new RepoApi();
            api.getMyRepos().whenComplete((res, err) -> {
                if (err != null) {
                    System.out.println("ERROR OCCURRED:");
                    Throwable cause = err;
                    while (cause.getCause() != null && cause != cause.getCause()) {
                        System.out.println("CAUSE: " + cause.getClass().getName() + " -> " + cause.getMessage());
                        cause = cause.getCause();
                    }
                    System.out.println("ROOT CAUSE: " + cause.getClass().getName() + " -> " + cause.getMessage());
                    
                    String msg = cause.getMessage() != null ? cause.getMessage() : "Unknown error";
                    System.out.println("EXTRACTED MESSAGE: " + msg);
                } else {
                    System.out.println("SUCCESS: " + res);
                }
            }).join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
