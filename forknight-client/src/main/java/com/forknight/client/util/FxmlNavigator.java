package com.forknight.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class FxmlNavigator {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void navigateTo(String fxmlFile) {
        navigateToWithController(fxmlFile);
    }

    public static <T> T navigateToWithController(String fxmlFile) {
        try {
            URL resource = FxmlNavigator.class.getResource("/fxml/" + fxmlFile);
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxmlFile);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            
            String theme = FxmlNavigator.class.getResource("/css/theme-dark.css").toExternalForm();
            String components = FxmlNavigator.class.getResource("/css/components.css").toExternalForm();

            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, 1000, 700);
                scene.getStylesheets().addAll(theme, components);
                primaryStage.setScene(scene);
            } else {
                Scene currentScene = primaryStage.getScene();
                if (!currentScene.getStylesheets().contains(theme)) {
                    currentScene.getStylesheets().addAll(theme, components);
                }
                currentScene.setRoot(root);
            }
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load view: " + fxmlFile, e);
        }
    }
}
