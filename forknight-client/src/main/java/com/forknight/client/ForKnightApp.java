package com.forknight.client;

import com.forknight.client.config.SessionManager;
import com.forknight.client.config.ThreadPools;
import com.forknight.client.util.FxmlNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class ForKnightApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        ThreadPools.init();
        SessionManager.getInstance().loadSavedSession();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ForKnight — Code Marketplace");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        
        FxmlNavigator.setPrimaryStage(primaryStage);
        if (SessionManager.getInstance().isLoggedIn()) {
            FxmlNavigator.navigateTo("dashboard.fxml");
        } else {
            FxmlNavigator.navigateTo("login.fxml");
        }
        
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        ThreadPools.shutdown();
    }
}
