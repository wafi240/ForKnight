package com.forknight.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestSubScene extends Application {
    public static void main(String[] args) { launch(args); }
    @Override
    public void start(Stage stage) {
        StackPane outer = new StackPane();
        outer.setPrefSize(1000, 800);
        
        StackPane inner = new StackPane();
        SubScene sub = new SubScene(new javafx.scene.Group(), 100, 100);
        sub.setFill(Color.RED);
        sub.widthProperty().bind(inner.widthProperty());
        sub.heightProperty().bind(inner.heightProperty());
        inner.getChildren().add(sub);
        
        outer.getChildren().add(inner);
        
        stage.setScene(new Scene(outer));
        stage.show();
        
        System.out.println("Inner size: " + inner.getWidth() + "x" + inner.getHeight());
        System.out.println("Sub size: " + sub.getWidth() + "x" + sub.getHeight());
        System.exit(0);
    }
}
