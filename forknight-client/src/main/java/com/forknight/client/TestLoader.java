package com.forknight.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.net.URL;

public class TestLoader {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                URL resource = TestLoader.class.getResource("/fxml/repo_detail.fxml");
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                System.out.println("Loaded successfully.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.exit();
            }
        });
    }
}
