package com.forknight.client.controller;

import com.forknight.client.api.RepoApi;
import com.forknight.client.util.AlertHelper;
import com.forknight.client.util.FxmlNavigator;
import com.forknight.client.util.TooltipHelper;
import com.forknight.common.dto.repo.RepoCreateRequest;
import com.forknight.common.dto.repo.RepoDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class MyReposController {

    @FXML private VBox reposContainer;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingSpinner;

    private final RepoApi repoApi = new RepoApi();

    @FXML
    public void initialize() {
        loadMyRepos();
    }

    @FXML
    public void loadMyRepos() {
        loadingSpinner.setVisible(true);
        statusLabel.setText("Fetching your repositories...");
        reposContainer.getChildren().clear();

        repoApi.getMyRepos().whenComplete((repos, error) -> {
            Platform.runLater(() -> {
                loadingSpinner.setVisible(false);
                if (error != null) {
                    statusLabel.setText("Failed to load repositories: " + extractMessage(error));
                } else if (repos == null || repos.isEmpty()) {
                    statusLabel.setText("You don't have any repositories yet. Create one to get started!");
                } else {
                    statusLabel.setText("Showing " + repos.size() + " repositories");
                    renderRepoCards(repos);
                }
            });
        });
    }

    private void renderRepoCards(List<RepoDTO> repos) {
        reposContainer.getChildren().clear();
        for (RepoDTO repo : repos) {
            HBox card = new HBox(15);
            card.getStyleClass().add("repo-card");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(15));

            VBox infoBox = new VBox(5);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            HBox titleRow = new HBox(10);
            titleRow.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label("📁 " + repo.name());
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

            Label privacyBadge = new Label(repo.isPublic() ? "Public" : "Private");
            privacyBadge.getStyleClass().add(repo.isPublic() ? "status-badge-approved" : "status-badge-pending");
            TooltipHelper.install(privacyBadge, repo.isPublic()
                    ? "This repository is public — anyone can view it"
                    : "This repository is private — only you can see it");

            Label licenseBadge = new Label(repo.licenseType() != null ? repo.licenseType() : "MIT");
            licenseBadge.getStyleClass().add("badge");
            TooltipHelper.install(licenseBadge, "License: " + (repo.licenseType() != null ? repo.licenseType() : "MIT") + " — governs how others may use this code");

            titleRow.getChildren().addAll(nameLabel, privacyBadge, licenseBadge);

            Label descLabel = new Label(repo.description() != null ? repo.description() : "No description provided.");
            descLabel.getStyleClass().add("label-subtitle");
            descLabel.setWrapText(true);

            Label metaLabel = new Label(String.format("⭐ %d stars   🍴 %d forks   Branch: %s",
                    repo.starsCount(), repo.forksCount(), repo.defaultBranch()));
            metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            TooltipHelper.install(metaLabel, "Stars received, times forked, and the default branch name");

            infoBox.getChildren().addAll(titleRow, descLabel, metaLabel);

            Button openBtn = new Button("Open Repo →");
            openBtn.getStyleClass().add("button");
            openBtn.setOnAction(e -> openRepoDetail(repo.id()));
            TooltipHelper.install(openBtn, "Open '" + repo.name() + "' — browse files, commits, pull requests, and more");

            card.getChildren().addAll(infoBox, openBtn);
            reposContainer.getChildren().add(card);
        }
    }

    private void openRepoDetail(Long repoId) {
        RepoDetailController controller = FxmlNavigator.navigateToWithController("repo_detail.fxml");
        if (controller != null) {
            controller.initRepo(repoId);
        }
    }

    @FXML
    private void handleNewRepoDialog() {
        Dialog<RepoCreateRequest> dialog = new Dialog<>();
        dialog.setTitle("Create New Repository");
        dialog.setHeaderText("Create a new ForKnight Git repository");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().addAll(
                getClass().getResource("/css/theme-dark.css").toExternalForm(),
                getClass().getResource("/css/components.css").toExternalForm()
        );

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Repository name (e.g. awesome-engine)");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Short description of your project");
        descArea.setPrefRowCount(3);

        CheckBox publicCheck = new CheckBox("Make repository public");
        publicCheck.setSelected(true);

        ComboBox<String> licenseCombo = new ComboBox<>();
        licenseCombo.getItems().addAll("MIT", "Apache-2.0", "GPL-3.0", "BSD-3-Clause", "Proprietary", "NONE");
        licenseCombo.setValue("MIT");

        TextField githubField = new TextField();
        githubField.setPromptText("Optional remote GitHub URL (e.g. https://github.com/owner/repo)");

        grid.add(new Label("Repo Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Visibility:"), 0, 2);
        grid.add(publicCheck, 1, 2);
        grid.add(new Label("License:"), 0, 3);
        grid.add(licenseCombo, 1, 3);
        grid.add(new Label("GitHub URL:"), 0, 4);
        grid.add(githubField, 1, 4);

        pane.setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText() != null ? nameField.getText().trim() : "";
                if (name.isEmpty()) {
                    return null;
                }
                return new RepoCreateRequest(
                        name,
                        descArea.getText(),
                        publicCheck.isSelected(),
                        licenseCombo.getValue(),
                        githubField.getText(),
                        false,
                        BigDecimal.ZERO
                );
            }
            return null;
        });

        Optional<RepoCreateRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            loadingSpinner.setVisible(true);
            statusLabel.setText("Creating repository '" + req.name() + "' on server...");

            repoApi.createRepo(req).whenComplete((created, error) -> {
                Platform.runLater(() -> {
                    loadingSpinner.setVisible(false);
                    if (error != null) {
                        AlertHelper.showError("Creation Failed", extractMessage(error));
                    } else {
                        AlertHelper.showSuccess("Success", "Repository '" + created.name() + "' initialized successfully!");
                        loadMyRepos();
                    }
                });
            });
        });
    }

    private final com.forknight.client.api.ContribApi contribApi = new com.forknight.client.api.ContribApi();
    private final com.forknight.client.api.CopyLicenseApi copyLicenseApi = new com.forknight.client.api.CopyLicenseApi();

    @FXML
    private void handleShowRequestsInbox() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Repository Requests Inbox");
        dialog.setHeaderText("Manage Access & Licensing for Your Repositories");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        try {
            pane.getStylesheets().addAll(
                    getClass().getResource("/css/theme-dark.css").toExternalForm(),
                    getClass().getResource("/css/components.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        TabPane tabPane = new TabPane();
        tabPane.setPrefWidth(650);
        tabPane.setPrefHeight(450);

        // Tab 1: Contribution Requests
        Tab contribTab = new Tab("👥 Contribution Access Requests");
        contribTab.setClosable(false);
        VBox contribList = new VBox(10);
        contribList.setPadding(new Insets(10));
        ScrollPane contribScroll = new ScrollPane(contribList);
        contribScroll.setFitToWidth(true);
        contribTab.setContent(contribScroll);

        // Tab 2: Copy / License Requests
        Tab copyTab = new Tab("📜 Copy / Licensing Requests");
        copyTab.setClosable(false);
        VBox copyList = new VBox(10);
        copyList.setPadding(new Insets(10));
        ScrollPane copyScroll = new ScrollPane(copyList);
        copyScroll.setFitToWidth(true);
        copyTab.setContent(copyScroll);

        tabPane.getTabs().addAll(contribTab, copyTab);
        pane.setContent(tabPane);

        // Load Contribution Requests
        Runnable loadContribs = () -> {
            contribList.getChildren().clear();
            contribList.getChildren().add(new Label("Loading contribution requests..."));
            contribApi.getIncomingRequests().whenComplete((requests, err) -> {
                Platform.runLater(() -> {
                    contribList.getChildren().clear();
                    if (requests == null || requests.isEmpty()) {
                        contribList.getChildren().add(new Label("No incoming contribution requests."));
                    } else {
                        for (com.forknight.common.dto.contrib.ContribRequestDTO cr : requests) {
                            HBox card = new HBox(12);
                            card.getStyleClass().add("card");
                            card.setAlignment(Pos.CENTER_LEFT);
                            card.setPadding(new Insets(10));

                            VBox info = new VBox(4);
                            HBox.setHgrow(info, Priority.ALWAYS);

                            Label title = new Label("@" + cr.requesterUsername() + " → " + cr.repoName());
                            title.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

                            Label msg = new Label("Message: " + (cr.message() != null ? cr.message() : "No message provided."));
                            msg.getStyleClass().add("label-subtitle");

                            Label statusBadge = new Label("Status: " + cr.status());
                            statusBadge.getStyleClass().add("badge");

                            info.getChildren().addAll(title, msg, statusBadge);

                            if (cr.status() == com.forknight.common.enums.RequestStatus.PENDING) {
                                Button approveBtn = new Button("Approve Access");
                                approveBtn.getStyleClass().add("button");
                                TooltipHelper.install(approveBtn, "Approve this contribution request and grant @" + cr.requesterUsername() + " access to your repository");
                                approveBtn.setOnAction(ev -> {
                                    ChoiceDialog<com.forknight.common.enums.AccessLevel> accessChoice = new ChoiceDialog<>(
                                            com.forknight.common.enums.AccessLevel.WRITE,
                                            com.forknight.common.enums.AccessLevel.READ,
                                            com.forknight.common.enums.AccessLevel.WRITE,
                                            com.forknight.common.enums.AccessLevel.ADMIN
                                    );
                                    accessChoice.setTitle("Grant Access Level");
                                    accessChoice.setHeaderText("Choose permission level for @" + cr.requesterUsername());
                                    accessChoice.showAndWait().ifPresent(lvl -> {
                                        com.forknight.common.dto.contrib.ContribDecisionRequest decision = 
                                                new com.forknight.common.dto.contrib.ContribDecisionRequest(
                                                        com.forknight.common.enums.RequestStatus.APPROVED,
                                                        lvl,
                                                        "Access granted by repository owner."
                                                );
                                        contribApi.decideRequest(cr.id(), decision).whenComplete((res, error) -> {
                                            Platform.runLater(() -> {
                                                AlertHelper.showSuccess("Approved", "Granted " + lvl + " access to @" + cr.requesterUsername());
                                                dialog.close();
                                            });
                                        });
                                    });
                                });

                                Button denyBtn = new Button("Deny");
                                denyBtn.getStyleClass().add("button-secondary");
                                TooltipHelper.install(denyBtn, "Deny this contribution request — @" + cr.requesterUsername() + " will not receive access");
                                TooltipHelper.install(denyBtn, "Deny this contribution request — @" + cr.requesterUsername() + " will not receive access");
                                denyBtn.setOnAction(ev -> {
                                    com.forknight.common.dto.contrib.ContribDecisionRequest decision = 
                                            new com.forknight.common.dto.contrib.ContribDecisionRequest(
                                                    com.forknight.common.enums.RequestStatus.DENIED,
                                                    null,
                                                    "Access denied by owner."
                                            );
                                    contribApi.decideRequest(cr.id(), decision).whenComplete((res, error) -> {
                                        Platform.runLater(() -> {
                                            AlertHelper.showSuccess("Denied", "Request was denied.");
                                            dialog.close();
                                        });
                                    });
                                });

                                card.getChildren().addAll(info, approveBtn, denyBtn);
                            } else {
                                card.getChildren().add(info);
                            }
                            contribList.getChildren().add(card);
                        }
                    }
                });
            });
        };

        // Load Copy / Licensing Requests
        Runnable loadCopies = () -> {
            copyList.getChildren().clear();
            copyList.getChildren().add(new Label("Loading copy requests..."));
            copyLicenseApi.getIncomingRequests().whenComplete((requests, err) -> {
                Platform.runLater(() -> {
                    copyList.getChildren().clear();
                    if (requests == null || requests.isEmpty()) {
                        copyList.getChildren().add(new Label("No incoming copy/license requests."));
                    } else {
                        for (com.forknight.common.dto.licensing.CopyLicenseRequestDTO clr : requests) {
                            HBox card = new HBox(12);
                            card.getStyleClass().add("card");
                            card.setAlignment(Pos.CENTER_LEFT);
                            card.setPadding(new Insets(10));

                            VBox info = new VBox(4);
                            HBox.setHgrow(info, Priority.ALWAYS);

                            Label title = new Label("@" + clr.requesterUsername() + " requests copy of " + clr.repoName());
                            title.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

                            Label msg = new Label("Purpose: " + (clr.message() != null ? clr.message() : "No message provided."));
                            msg.getStyleClass().add("label-subtitle");

                            Label statusBadge = new Label("Status: " + clr.status());
                            statusBadge.getStyleClass().add("badge");

                            info.getChildren().addAll(title, msg, statusBadge);

                            if (clr.status() == com.forknight.common.enums.CopyLicenseStatus.PENDING) {
                                Button freeBtn = new Button("Approve Free (Open Source)");
                                freeBtn.getStyleClass().add("button");
                                TooltipHelper.install(freeBtn, "Grant @" + clr.requesterUsername() + " a free open-source copy license — no fee charged");
                                freeBtn.setOnAction(ev -> {
                                    com.forknight.common.dto.licensing.CopyLicenseDecisionRequest decision = 
                                            new com.forknight.common.dto.licensing.CopyLicenseDecisionRequest(
                                                    com.forknight.common.enums.CopyLicenseStatus.APPROVED_FREE,
                                                    BigDecimal.ZERO,
                                                    "Granted open-source fork/copy permission."
                                            );
                                    copyLicenseApi.decideRequest(clr.id(), decision).whenComplete((res, error) -> {
                                        Platform.runLater(() -> {
                                            AlertHelper.showSuccess("Approved", "Approved as Free/Open Source!");
                                            dialog.close();
                                        });
                                    });
                                });

                                Button paidBtn = new Button("Approve Paid ($)");
                                paidBtn.getStyleClass().add("button-secondary");
                                TooltipHelper.install(paidBtn, "Approve with a commercial license fee — you will set the price @" + clr.requesterUsername() + " must pay");
                                TooltipHelper.install(paidBtn, "Approve with a commercial license fee — you will set the price @" + clr.requesterUsername() + " must pay");
                                paidBtn.setOnAction(ev -> {
                                    TextInputDialog feeInput = new TextInputDialog("49.99");
                                    feeInput.setTitle("Commercial License Fee");
                                    feeInput.setHeaderText("Set commercial license fee (USD) for @" + clr.requesterUsername());
                                    feeInput.showAndWait().ifPresent(feeStr -> {
                                        try {
                                            BigDecimal fee = new BigDecimal(feeStr);
                                            com.forknight.common.dto.licensing.CopyLicenseDecisionRequest decision = 
                                                    new com.forknight.common.dto.licensing.CopyLicenseDecisionRequest(
                                                            com.forknight.common.enums.CopyLicenseStatus.APPROVED_PAID,
                                                            fee,
                                                            "Commercial license granted with fee: $" + fee
                                                    );
                                            copyLicenseApi.decideRequest(clr.id(), decision).whenComplete((res, error) -> {
                                                Platform.runLater(() -> {
                                                    AlertHelper.showSuccess("Approved Paid", "Commercial license set at $" + fee);
                                                    dialog.close();
                                                });
                                            });
                                        } catch (Exception ex) {
                                            AlertHelper.showError("Invalid Fee", "Please enter a valid monetary amount.");
                                        }
                                    });
                                });

                                Button denyBtn = new Button("Deny");
                                denyBtn.getStyleClass().add("button-danger");
                                TooltipHelper.install(denyBtn, "Deny this copy/license request — @" + clr.requesterUsername() + " will not receive permission to copy this repository");
                                TooltipHelper.install(denyBtn, "Deny this copy/license request — @" + clr.requesterUsername() + " will not receive permission to copy this repository");
                                denyBtn.setOnAction(ev -> {
                                    com.forknight.common.dto.licensing.CopyLicenseDecisionRequest decision = 
                                            new com.forknight.common.dto.licensing.CopyLicenseDecisionRequest(
                                                    com.forknight.common.enums.CopyLicenseStatus.DENIED,
                                                    null,
                                                    "Copy permission denied by owner."
                                            );
                                    copyLicenseApi.decideRequest(clr.id(), decision).whenComplete((res, error) -> {
                                        Platform.runLater(() -> {
                                            AlertHelper.showSuccess("Denied", "Copy request denied.");
                                            dialog.close();
                                        });
                                    });
                                });

                                card.getChildren().addAll(info, freeBtn, paidBtn, denyBtn);
                            } else {
                                card.getChildren().add(info);
                            }
                            copyList.getChildren().add(card);
                        }
                    }
                });
            });
        };

        loadContribs.run();
        loadCopies.run();
        dialog.showAndWait();
    }

    private String extractMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : "Unknown error";
    }
}
