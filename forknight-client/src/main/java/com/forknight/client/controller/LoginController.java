package com.forknight.client.controller;

import com.forknight.client.api.AuthApi;
import com.forknight.client.config.SessionManager;
import com.forknight.client.util.FxmlNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink;
    @FXML private ProgressIndicator loadingSpinner;

    private final AuthApi authApi = new AuthApi();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loadingSpinner.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        errorLabel.setVisible(false);
        loginButton.setDisable(true);
        loadingSpinner.setVisible(true);

        authApi.login(username, password).whenComplete((response, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    showError(extractErrorMessage(error));
                    loginButton.setDisable(false);
                    loadingSpinner.setVisible(false);
                } else {
                    SessionManager.getInstance().saveSession(response);
                    FxmlNavigator.navigateTo("dashboard.fxml");
                }
            });
        });
    }

    @FXML
    private void handleRegisterLink() {
        FxmlNavigator.navigateTo("register.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private String extractErrorMessage(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        String msg = cause.getMessage();
        if (msg == null || msg.isBlank()) {
            return "An unexpected error occurred.";
        }
        if (msg.contains("Connection refused") || msg.contains("ConnectException")) {
            return "Unable to connect to ForKnight server. Is it running on port 8080?";
        }
        return msg;
    }
}
