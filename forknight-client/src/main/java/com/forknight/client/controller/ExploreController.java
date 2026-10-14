package com.forknight.client.controller;

import com.forknight.client.api.RepoApi;
import com.forknight.client.config.ThreadPools;
import com.forknight.client.task.FileDownloadTask;
import com.forknight.client.util.AlertHelper;
import com.forknight.client.util.FxmlNavigator;
import com.forknight.client.util.TooltipHelper;
import com.forknight.common.dto.PagedResponse;
import com.forknight.common.dto.repo.RepoDTO;
import com.forknight.common.dto.repo.SingleFileDownloadRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class ExploreController {

    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private VBox exploreContainer;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private final RepoApi repoApi = new RepoApi();
    private int currentPage = 0;
    private final int pageSize = 10;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        loadExploreRepos();
    }

    @FXML
    public void handleSearch() {
        currentPage = 0;
        loadExploreRepos();
    }

    @FXML
    public void handleClearSearch() {
        searchField.clear();
        currentPage = 0;
        loadExploreRepos();
    }

    @FXML
    public void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadExploreRepos();
        }
    }

    @FXML
    public void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadExploreRepos();
        }
    }

    private void loadExploreRepos() {
        loadingSpinner.setVisible(true);
        statusLabel.setText("Discovering public repositories...");
        String query = searchField.getText();

        repoApi.exploreRepos(query, currentPage, pageSize).whenComplete((response, error) -> {
            Platform.runLater(() -> {
                loadingSpinner.setVisible(false);
                if (error != null) {
                    statusLabel.setText("Error loading repositories: " + extractMessage(error));
                } else if (response == null || response.content().isEmpty()) {
                    statusLabel.setText("No public repositories found.");
                    exploreContainer.getChildren().clear();
                } else {
                    totalPages = Math.max(1, response.totalPages());
                    pageInfoLabel.setText(String.format("Page %d of %d (%d total)",
                            currentPage + 1, totalPages, response.totalElements()));
                    prevPageButton.setDisable(currentPage <= 0);
                    nextPageButton.setDisable(currentPage >= totalPages - 1);

                    statusLabel.setText("Found " + response.totalElements() + " repositories");
                    renderExploreCards(response.content());
                }
            });
        });
    }

    private void renderExploreCards(java.util.List<RepoDTO> repos) {
        exploreContainer.getChildren().clear();
        for (RepoDTO repo : repos) {
            HBox card = new HBox(15);
            card.getStyleClass().add("repo-card");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(15));

            VBox infoBox = new VBox(5);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            HBox titleRow = new HBox(10);
            titleRow.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label("📦 " + repo.name());
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

            Label ownerBadge = new Label("by @" + repo.ownerUsername());
            ownerBadge.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
            TooltipHelper.install(ownerBadge, "View profile for @" + repo.ownerUsername());

            Label licenseBadge = new Label(repo.licenseType() != null ? repo.licenseType() : "MIT");
            licenseBadge.getStyleClass().add("badge");
            TooltipHelper.install(licenseBadge, "License: " + (repo.licenseType() != null ? repo.licenseType() : "MIT"));

            titleRow.getChildren().addAll(nameLabel, ownerBadge, licenseBadge);

            Label descLabel = new Label(repo.description() != null ? repo.description() : "No description provided.");
            descLabel.getStyleClass().add("label-subtitle");
            descLabel.setWrapText(true);

            Label metaLabel = new Label(String.format("⭐ %d stars   🍴 %d forks", repo.starsCount(), repo.forksCount()));
            metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            TooltipHelper.install(metaLabel, "Total stars and forks across the community");

            infoBox.getChildren().addAll(titleRow, descLabel, metaLabel);

            Button contribBtn = new Button("👥 Contrib");
            contribBtn.getStyleClass().add("button-secondary");
            TooltipHelper.install(contribBtn, "Request permission to contribute to this repository");
            contribBtn.setOnAction(e -> handleRequestContribution(repo));

            Button copyBtn = new Button("📜 Request Copy");
            copyBtn.getStyleClass().add("button-secondary");
            TooltipHelper.install(copyBtn, "Request a copy license to legally use or fork this repository");
            copyBtn.setOnAction(e -> handleRequestCopy(repo));

            Button inspectBtn = new Button("Inspect Repo →");
            inspectBtn.getStyleClass().add("button");
            TooltipHelper.install(inspectBtn, "Open '" + repo.name() + "' to browse files, commits, and pull requests");
            inspectBtn.setOnAction(e -> openRepoDetail(repo.id()));

            HBox actionsBox = new HBox(8, contribBtn, copyBtn, inspectBtn);
            actionsBox.setAlignment(Pos.CENTER_RIGHT);

            card.getChildren().addAll(infoBox, actionsBox);
            exploreContainer.getChildren().add(card);
        }
    }

    private final com.forknight.client.api.ContribApi contribApi = new com.forknight.client.api.ContribApi();
    private final com.forknight.client.api.CopyLicenseApi copyLicenseApi = new com.forknight.client.api.CopyLicenseApi();

    private void handleRequestContribution(RepoDTO repo) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Request Contribution Access");
        dialog.setHeaderText("Request to contribute to " + repo.name() + " (Owner: @" + repo.ownerUsername() + ")");
        dialog.setContentText("Introduce yourself and what you'd like to work on:");

        dialog.showAndWait().ifPresent(msg -> {
            if (msg.trim().isEmpty()) {
                AlertHelper.showError("Required", "Please provide a brief message to the repository owner.");
                return;
            }
            com.forknight.common.dto.contrib.ContribCreateRequest req = 
                    new com.forknight.common.dto.contrib.ContribCreateRequest(repo.id(), msg.trim());

            contribApi.requestContribution(req).whenComplete((res, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Request Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Request Sent", "Contribution request sent to @" + repo.ownerUsername() + "!");
                    }
                });
            });
        });
    }

    private void handleRequestCopy(RepoDTO repo) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Request Copy / License");
        dialog.setHeaderText("Request permission to copy / fork " + repo.name() + " (Owner: @" + repo.ownerUsername() + ")");
        dialog.setContentText("Describe intended usage (Open Source / Commercial):");

        dialog.showAndWait().ifPresent(msg -> {
            if (msg.trim().isEmpty()) {
                AlertHelper.showError("Required", "Please state your intended use.");
                return;
            }
            com.forknight.common.dto.licensing.CopyLicenseCreateRequest req = 
                    new com.forknight.common.dto.licensing.CopyLicenseCreateRequest(repo.id(), msg.trim());

            copyLicenseApi.requestCopy(req).whenComplete((res, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Request Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Request Sent", "Copy/license request submitted to @" + repo.ownerUsername() + "!");
                    }
                });
            });
        });
    }

    private void openRepoDetail(Long repoId) {
        RepoDetailController controller = FxmlNavigator.navigateToWithController("repo_detail.fxml");
        if (controller != null) {
            controller.initRepo(repoId);
        }
    }

    @FXML
    private void handleDirectSingleFileDownload() {
        Dialog<SingleFileDownloadRequest> dialog = new Dialog<>();
        dialog.setTitle("Single-File Direct Downloader");
        dialog.setHeaderText("Bypass Git Clone: Fetch any file directly from GitHub");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().addAll(
                getClass().getResource("/css/theme-dark.css").toExternalForm(),
                getClass().getResource("/css/components.css").toExternalForm()
        );

        ButtonType downloadBtnType = new ButtonType("Download", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(downloadBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 100, 10, 10));

        TextField ownerField = new TextField();
        ownerField.setPromptText("e.g. torvalds, facebook, spring-projects");

        TextField repoField = new TextField();
        repoField.setPromptText("e.g. linux, react, spring-boot");

        TextField branchField = new TextField("main");
        branchField.setPromptText("Branch or tag (default: main)");

        TextField pathField = new TextField();
        pathField.setPromptText("File path (e.g. README.md or src/main/java/App.java)");

        PasswordField tokenField = new PasswordField();
        tokenField.setPromptText("Optional Personal Access Token (for private repos)");

        grid.add(new Label("GitHub Owner:"), 0, 0);
        grid.add(ownerField, 1, 0);
        grid.add(new Label("Repository:"), 0, 1);
        grid.add(repoField, 1, 1);
        grid.add(new Label("Branch / Ref:"), 0, 2);
        grid.add(branchField, 1, 2);
        grid.add(new Label("Target File Path:"), 0, 3);
        grid.add(pathField, 1, 3);
        grid.add(new Label("GitHub Token:"), 0, 4);
        grid.add(tokenField, 1, 4);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == downloadBtnType) {
                if (ownerField.getText().isBlank() || repoField.getText().isBlank() || pathField.getText().isBlank()) {
                    return null;
                }
                return new SingleFileDownloadRequest(
                        ownerField.getText().trim(),
                        repoField.getText().trim(),
                        branchField.getText().trim(),
                        pathField.getText().trim(),
                        tokenField.getText()
                );
            }
            return null;
        });

        Optional<SingleFileDownloadRequest> optReq = dialog.showAndWait();
        optReq.ifPresent(req -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose Save Directory for Downloaded File");
            File destinationDir = chooser.showDialog(pane.getScene().getWindow());

            if (destinationDir != null) {
                launchDownloadTask(req, destinationDir);
            }
        });
    }

    private void launchDownloadTask(SingleFileDownloadRequest request, File destinationDir) {
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Downloading Single File");
        progressAlert.setHeaderText("Fetching file without cloning repo...");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(350);
        Label statusLbl = new Label("Connecting...");
        VBox box = new VBox(10, progressBar, statusLbl);
        box.setPadding(new Insets(10));
        progressAlert.getDialogPane().setContent(box);

        FileDownloadTask downloadTask = new FileDownloadTask(request, destinationDir);
        progressBar.progressProperty().bind(downloadTask.progressProperty());
        statusLbl.textProperty().bind(downloadTask.messageProperty());

        downloadTask.setOnSucceeded(ev -> {
            Path savedPath = downloadTask.getValue();
            progressAlert.close();
            AlertHelper.showSuccess("Download Completed", "File saved successfully to:\n" + savedPath.toAbsolutePath());
        });

        downloadTask.setOnFailed(ev -> {
            progressAlert.close();
            Throwable ex = downloadTask.getException();
            AlertHelper.showError("Download Failed", extractMessage(ex));
        });

        ThreadPools.IO_POOL.submit(downloadTask);
        progressAlert.show();
    }

    private String extractMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : "Unknown error";
    }
}
