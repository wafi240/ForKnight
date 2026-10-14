package com.forknight.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class TestFxml extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/repo_detail.fxml"));
            loader.load();
            System.out.println("LOADED SUCCESSFULLY");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
