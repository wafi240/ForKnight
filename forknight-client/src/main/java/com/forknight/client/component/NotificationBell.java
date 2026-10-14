package com.forknight.client.component;

import com.forknight.client.api.NotificationApi;
import com.forknight.common.dto.notification.NotificationDTO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import com.forknight.client.util.TooltipHelper;

import java.util.List;

public class NotificationBell extends StackPane {

    private final Button bellButton = new Button("🔔");
    private final Label badge = new Label("0");
    private final NotificationApi notificationApi = new NotificationApi();

    public NotificationBell() {
        bellButton.getStyleClass().add("button-secondary");
        bellButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 10;");

        badge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; " +
                       "-fx-font-weight: bold; -fx-padding: 1 5; -fx-background-radius: 10; -fx-border-radius: 10;");
        badge.setVisible(false);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);

        getChildren().addAll(bellButton, badge);

        bellButton.setOnAction(e -> showNotificationsDialog());
        refreshUnreadCount();
    }

    public void refreshUnreadCount() {
        notificationApi.getUnreadNotifications().whenComplete((notifs, error) -> {
            Platform.runLater(() -> {
                if (notifs != null && !notifs.isEmpty()) {
                    badge.setText(String.valueOf(notifs.size()));
                    badge.setVisible(true);
                } else {
                    badge.setVisible(false);
                }
            });
        });
    }

    private void showNotificationsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Notifications");
        dialog.setHeaderText("Your Recent Alerts & Updates");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        try {
            pane.getStylesheets().addAll(
                    getClass().getResource("/css/theme-dark.css").toExternalForm(),
                    getClass().getResource("/css/components.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        contentBox.setPrefWidth(450);
        contentBox.setPrefHeight(350);

        HBox topActions = new HBox(10);
        topActions.setAlignment(Pos.CENTER_RIGHT);
        Button markAllBtn = new Button("Mark all as read");
        markAllBtn.getStyleClass().add("button-secondary");
        TooltipHelper.install(markAllBtn, "Mark all notifications as read and dismiss the alert badge");
        topActions.getChildren().add(markAllBtn);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox listContainer = new VBox(8);
        listContainer.setPadding(new Insets(5));
        scrollPane.setContent(listContainer);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        contentBox.getChildren().addAll(topActions, scrollPane);
        pane.setContent(contentBox);

        Runnable loadItems = () -> {
            listContainer.getChildren().clear();
            listContainer.getChildren().add(new Label("Loading notifications..."));

            notificationApi.getNotifications().whenComplete((notifs, error) -> {
                Platform.runLater(() -> {
                    listContainer.getChildren().clear();
                    if (error != null) {
                        listContainer.getChildren().add(new Label("Error loading notifications."));
                    } else if (notifs == null || notifs.isEmpty()) {
                        listContainer.getChildren().add(new Label("No notifications right now."));
                    } else {
                        for (NotificationDTO n : notifs) {
                            HBox item = new HBox(10);
                            item.getStyleClass().add("card");
                            item.setAlignment(Pos.CENTER_LEFT);
                            item.setPadding(new Insets(8, 12, 8, 12));

                            VBox textVBox = new VBox(3);
                            HBox.setHgrow(textVBox, javafx.scene.layout.Priority.ALWAYS);

                            Label titleLbl = new Label(n.title());
                            titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (n.isRead() ? "#94a3b8" : "#e2e8f0") + ";");

                            Label msgLbl = new Label(n.message());
                            msgLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                            msgLbl.setWrapText(true);

                            textVBox.getChildren().addAll(titleLbl, msgLbl);

                            if (!n.isRead()) {
                                Button readBtn = new Button("✓");
                                readBtn.getStyleClass().add("button-secondary");
                                TooltipHelper.install(readBtn, "Mark this notification as read");
                                readBtn.setOnAction(ev -> {
                                    notificationApi.markAsRead(n.id()).whenComplete((v, err) -> {
                                        Platform.runLater(this::refreshUnreadCount);
                                    });
                                    item.setOpacity(0.6);
                                    readBtn.setDisable(true);
                                });
                                item.getChildren().addAll(textVBox, readBtn);
                            } else {
                                item.getChildren().add(textVBox);
                            }

                            listContainer.getChildren().add(item);
                        }
                    }
                });
            });
        };

        markAllBtn.setOnAction(ev -> {
            notificationApi.markAllAsRead().whenComplete((v, err) -> {
                Platform.runLater(() -> {
                    refreshUnreadCount();
                    loadItems.run();
                });
            });
        });

        loadItems.run();
        dialog.showAndWait();
    }
}
