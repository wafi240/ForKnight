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

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField displayNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink loginLink;
    @FXML private ProgressIndicator loadingSpinner;

    private final AuthApi authApi = new AuthApi();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loadingSpinner.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String displayName = displayNameField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || displayName.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        if (!email.contains("@")) {
            showError("Invalid email format.");
            return;
        }

        errorLabel.setVisible(false);
        registerButton.setDisable(true);
        loadingSpinner.setVisible(true);

        authApi.register(username, email, password, displayName).whenComplete((response, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    showError(extractErrorMessage(error));
                    registerButton.setDisable(false);
                    loadingSpinner.setVisible(false);
                } else {
                    SessionManager.getInstance().saveSession(response);
                    FxmlNavigator.navigateTo("dashboard.fxml");
                }
            });
        });
    }

    @FXML
    private void handleLoginLink() {
        FxmlNavigator.navigateTo("login.fxml");
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
            return "An unexpected error occurred during registration.";
        }
        if (msg.contains("Connection refused") || msg.contains("ConnectException")) {
            return "Unable to connect to ForKnight server. Is it running on port 8080?";
        }
        return msg;
    }
}
