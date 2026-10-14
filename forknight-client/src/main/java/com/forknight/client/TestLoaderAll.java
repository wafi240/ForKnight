package com.forknight.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.net.URL;

public class TestLoaderAll {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                String[] files = {"my_repos.fxml", "explore.fxml", "marketplace.fxml", "dashboard.fxml"};
                for(String file : files) {
                    URL resource = TestLoaderAll.class.getResource("/fxml/" + file);
                    FXMLLoader loader = new FXMLLoader(resource);
                    Parent root = loader.load();
                    System.out.println(file + " Loaded successfully.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.exit();
            }
        });
    }
}
