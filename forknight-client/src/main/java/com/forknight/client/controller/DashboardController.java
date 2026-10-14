package com.forknight.client.controller;

import com.forknight.client.config.SessionManager;
import com.forknight.client.util.FxmlNavigator;
import com.forknight.client.util.TooltipHelper;
import com.forknight.common.dto.auth.LoginResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
    @FXML private com.forknight.client.component.NotificationBell notificationBell;
    @FXML private TabPane mainTabPane;
    @FXML private Tab myReposTab;
    @FXML private Tab exploreTab;
    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        LoginResponse user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String name = (user.displayName() != null && !user.displayName().isBlank()) 
                    ? user.displayName() 
                    : user.username();
            welcomeLabel.setText("Welcome, " + name + "!");
            userRoleLabel.setText(user.role() != null ? user.role() : "USER");
        }
        if (notificationBell != null) {
            TooltipHelper.install(notificationBell, "Open your notifications inbox");
        }
        if (notificationBell != null) {
            TooltipHelper.install(notificationBell, "Open your notifications inbox");
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        FxmlNavigator.navigateTo("login.fxml");
    }
}
