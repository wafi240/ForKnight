package com.forknight.client.controller;

import com.forknight.client.api.RepoApi;
import com.forknight.client.api.SecretScanApi;
import com.forknight.client.config.ThreadPools;
import com.forknight.client.task.SecretScanTask;
import com.forknight.client.util.AlertHelper;
import com.forknight.client.util.FxmlNavigator;
import com.forknight.client.util.TooltipHelper;
import com.forknight.common.dto.repo.CommitDTO;
import com.forknight.common.dto.repo.FileContentDTO;
import com.forknight.common.dto.repo.FileNodeDTO;
import com.forknight.common.dto.repo.RepoDTO;
import com.forknight.common.dto.secret.SecretScanResultDTO;
import com.forknight.client.component.AiReviewCardComponent;
import com.forknight.client.component.DiffViewerComponent;
import com.forknight.common.dto.ai.AiReviewDTO;
import com.forknight.common.dto.ai.AiReviewRequest;
import com.forknight.common.dto.diff.DiffEntryDTO;
import com.forknight.common.dto.pr.PRCreateRequest;
import com.forknight.common.dto.pr.PRStatus;
import com.forknight.common.dto.pr.PullRequestDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class RepoDetailController {

    @FXML private Label repoTitleLabel;
    @FXML private Label visibilityBadge;
    @FXML private Label licenseBadge;
    @FXML private Label repoStatsLabel;
    @FXML private Label repoDescriptionLabel;

    @FXML private TreeView<FileNodeDTO> fileTreeView;
    @FXML private Label currentFileLabel;
    @FXML private Label fileSizeLabel;
    @FXML private TextArea codeViewer;
    @FXML private Button downloadCurrentFileButton;

    @FXML private ListView<String> commitsListView;

    @FXML private Label scannerBanner;
    @FXML private TableView<SecretScanResultDTO> secretsTable;
    @FXML private TableColumn<SecretScanResultDTO, String> colSeverity;
    @FXML private TableColumn<SecretScanResultDTO, String> colRule;
    @FXML private TableColumn<SecretScanResultDTO, String> colFile;
    @FXML private TableColumn<SecretScanResultDTO, Integer> colLine;
    @FXML private TableColumn<SecretScanResultDTO, String> colSnippet;

    @FXML private TextArea newCommentArea;
    @FXML private CheckBox isSuggestionCheck;
    @FXML private VBox commentsContainer;

    @FXML private StackPane graph3DContainer;
    @FXML private javafx.scene.canvas.Canvas graph2DCanvas;
    @FXML private ScrollPane graph2DScroll;
    @FXML private javafx.scene.control.ToggleButton graphToggleBtn;
    @FXML private Label graphModeLabel;
    
    @FXML private Label graphStatsLabel;
    @FXML private Button btnResetView;
    @FXML private javafx.scene.layout.Pane graphOverlayPane;

    @FXML private Label inspectorHashLabel;
    @FXML private Label inspectorBranchLabel;
    @FXML private Label inspectorAuthorLabel;
    @FXML private Label inspectorDateLabel;
    @FXML private TextArea inspectorMessageArea;

    private boolean is3DMode = false;

    @FXML private VBox prContainer;
    @FXML private ComboBox<String> aiModelCombo;
    @FXML private PasswordField aiApiKeyField;
    @FXML private TextArea aiCodeInputArea;
    @FXML private ProgressIndicator aiLoadingSpinner;
    @FXML private VBox aiReviewContainer;

    private com.forknight.client.component.GitGraph3DView gitGraph3DView;
    private Long currentRepoId;
    private RepoDTO currentRepo;
    private FileNodeDTO selectedFileNode;
    private final RepoApi repoApi = new RepoApi();
    private final SecretScanApi secretScanApi = new SecretScanApi();
    private final com.forknight.client.api.AiReviewApi aiReviewApi = new com.forknight.client.api.AiReviewApi();
    private final com.forknight.client.api.MarketplaceApi marketplaceApi = new com.forknight.client.api.MarketplaceApi();

    @FXML private javafx.scene.layout.VBox lockedOverlay;
    @FXML private javafx.scene.control.TabPane detailTabPane;
    @FXML private javafx.scene.control.Label lockedPriceLabel;
    @FXML private javafx.scene.control.Label lockedDescriptionLabel;

    @FXML
    private void handleBuyRepo() {
        if (currentRepoId == null || currentRepo == null) return;
        marketplaceApi.purchaseRepo(currentRepoId).whenComplete((res, err) -> {
            javafx.application.Platform.runLater(() -> {
                if (err != null) {
                    com.forknight.client.util.AlertHelper.showError("Purchase Failed", extractMessage(err));
                } else {
                    com.forknight.client.util.AlertHelper.showSuccess("Success", "Repository purchased successfully!");
                    initRepo(currentRepoId);
                }
            });
        });
    }
    private final com.forknight.client.api.ContribApi contribApi = new com.forknight.client.api.ContribApi();
    private final com.forknight.client.api.CopyLicenseApi copyLicenseApi = new com.forknight.client.api.CopyLicenseApi();
    private final com.forknight.client.api.PullRequestApi prApi = new com.forknight.client.api.PullRequestApi();
    private final com.forknight.client.api.CommentApi commentApi = new com.forknight.client.api.CommentApi();

    @FXML
    public void initialize() {
        setupFileTree();
        setupSecretsTable();
        setup3DGraph();
        if (aiModelCombo != null) {
            aiModelCombo.getItems().addAll("intelligent-reviewer-v2", "gpt-4o", "gpt-3.5-turbo", "claude-3-5-sonnet");
            aiModelCombo.setValue("intelligent-reviewer-v2");
        }
    }

    public void initRepo(Long repoId) {
        this.currentRepoId = repoId;
        loadRepoDetails();
        refreshFileTree();
        loadCommits();
        loadGraph2D();
        loadPreviousScanResults();
        loadComments();
        loadPullRequests();
    }

    private void loadRepoDetails() {
        repoApi.getRepoById(currentRepoId).whenComplete((repo, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    AlertHelper.showError("Error", "Could not load repository: " + extractMessage(error));
                } else if (repo != null) {
                    this.currentRepo = repo;
                    repoTitleLabel.setText(repo.name());
                    visibilityBadge.setText(repo.isPublic() ? "Public" : "Private");
                    licenseBadge.setText(repo.licenseType() != null ? repo.licenseType() : "MIT");
                    repoStatsLabel.setText(String.format("⭐ %d stars | 🍴 %d forks", repo.starsCount(), repo.forksCount()));
                    repoDescriptionLabel.setText(repo.description() != null ? repo.description() : "No description provided.");
                }
            });
        });
    }

    private void setupFileTree() {
        fileTreeView.setShowRoot(false);
        fileTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(FileNodeDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText((item.isDirectory() ? "📁 " : "📄 ") + item.name());
                    TooltipHelper.install(this, item.isDirectory() ? "Directory: " + item.path() : "File: " + item.path());
                }
            }
        });

        fileTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null) {
                FileNodeDTO node = newVal.getValue();
                if (!node.isDirectory()) {
                    loadFileContent(node);
                }
            }
        });
    }

    @FXML
    public void refreshFileTree() {
        if (currentRepoId == null) return;

        repoApi.getFileTree(currentRepoId).whenComplete((nodes, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    codeViewer.setText("Failed to load file tree: " + extractMessage(error));
                    String errMsg = extractMessage(error).toLowerCase();
                    if (errMsg.contains("access") || errMsg.contains("purchase")) {
                        if (detailTabPane != null && lockedOverlay != null && currentRepo != null) {
                            detailTabPane.setVisible(false);
                            lockedOverlay.setVisible(true);
                            if (lockedDescriptionLabel != null) lockedDescriptionLabel.setText(currentRepo.description() != null ? currentRepo.description() : "Premium Repository");
                            if (lockedPriceLabel != null) lockedPriceLabel.setText(String.format("$%.2f", currentRepo.salePrice()));
                        }
                    }
                } else if (nodes != null) {
                    if (detailTabPane != null) detailTabPane.setVisible(true);
                    if (lockedOverlay != null) lockedOverlay.setVisible(false);
                    TreeItem<FileNodeDTO> rootItem = new TreeItem<>(FileNodeDTO.directory("root", "", List.of()));
                    rootItem.setExpanded(true);
                    for (FileNodeDTO node : nodes) {
                        rootItem.getChildren().add(buildTreeItem(node));
                    }
                    fileTreeView.setRoot(rootItem);
                }
            });
        });
    }

    private TreeItem<FileNodeDTO> buildTreeItem(FileNodeDTO node) {
        TreeItem<FileNodeDTO> item = new TreeItem<>(node);
        if (node.isDirectory() && node.children() != null) {
            item.setExpanded(true);
            for (FileNodeDTO child : node.children()) {
                item.getChildren().add(buildTreeItem(child));
            }
        }
        return item;
    }

    private void loadFileContent(FileNodeDTO node) {
        this.selectedFileNode = node;
        currentFileLabel.setText(node.path());
        fileSizeLabel.setText("(" + node.size() + " bytes)");
        downloadCurrentFileButton.setDisable(false);
        codeViewer.setText("Loading file content...");

        repoApi.getFileContent(currentRepoId, node.path()).whenComplete((fileDto, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    codeViewer.setText("Error reading file: " + extractMessage(error));
                } else if (fileDto != null) {
                    if (fileDto.isBinary()) {
                        codeViewer.setText("[Binary file: " + fileDto.size() + " bytes - cannot preview as text]");
                    } else {
                        codeViewer.setText(fileDto.content());
                    }
                }
            });
        });
    }

    @FXML
    private void handleUploadFile() {
        if (currentRepoId == null) return;
        
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Select File to Upload");
        java.io.File file = chooser.showOpenDialog(repoTitleLabel.getScene().getWindow());
        if (file != null) {
            try {
                byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                com.forknight.common.dto.repo.FileUploadRequest req = new com.forknight.common.dto.repo.FileUploadRequest(file.getName(), base64);
                
                repoApi.uploadFile(currentRepoId, req).whenComplete((res, err) -> {
                    javafx.application.Platform.runLater(() -> {
                        if (err != null) {
                            com.forknight.client.util.AlertHelper.showError("Upload Failed", extractMessage(err));
                        } else {
                            com.forknight.client.util.AlertHelper.showSuccess("Success", "File uploaded successfully!");
                            refreshFileTree();
                        }
                    });
                });
            } catch (java.io.IOException e) {
                com.forknight.client.util.AlertHelper.showError("Error reading file", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDownloadSelectedFile() {
        if (selectedFileNode == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save File As");
        chooser.setInitialFileName(selectedFileNode.name());

        File saveTarget = chooser.showSaveDialog(repoTitleLabel.getScene().getWindow());
        if (saveTarget != null) {
            try {
                Files.writeString(saveTarget.toPath(), codeViewer.getText(), StandardCharsets.UTF_8);
                AlertHelper.showSuccess("File Downloaded", "Saved to:\n" + saveTarget.getAbsolutePath());
            } catch (Exception e) {
                AlertHelper.showError("Save Failed", "Could not save file: " + e.getMessage());
            }
        }
    }

    @FXML
    public void loadCommits() {
        if (currentRepoId == null) return;
        commitsListView.getItems().clear();

        repoApi.getCommitHistory(currentRepoId).whenComplete((commits, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    commitsListView.getItems().add("Error loading commits: " + extractMessage(error));
                } else if (commits == null || commits.isEmpty()) {
                    commitsListView.getItems().add("No commits found in repository.");
                } else {
                    for (CommitDTO c : commits) {
                        String entry = String.format("[%s] %s — %s (%s)",
                                c.shortHash(), c.message(), c.authorName(), c.timestamp().toString());
                        commitsListView.getItems().add(entry);
                    }
                }
            });
        });
    }

    private void setupSecretsTable() {
        colSeverity.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().severity().name()));
        colRule.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().ruleName()));
        colFile.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().filePath()));
        colLine.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().lineNumber()).asObject());
        colSnippet.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().maskedSnippet()));
    }

    private void loadPreviousScanResults() {
        if (currentRepoId == null) return;
        secretScanApi.getRepoScanResults(currentRepoId).whenComplete((results, error) -> {
            Platform.runLater(() -> {
                if (results != null && !results.isEmpty()) {
                    secretsTable.getItems().setAll(results);
                    scannerBanner.setText("⚠️ " + results.size() + " hardcoded secrets/credentials detected!");
                    scannerBanner.setStyle("-fx-text-fill: #F87171; -fx-font-weight: bold;");
                }
            });
        });
    }

    @FXML
    private void handleRunSecretScan() {
        if (currentRepoId == null) return;

        scannerBanner.setText("Scanning repository for secret leaks...");
        scannerBanner.setStyle("-fx-text-fill: #FBBF24;");

        SecretScanTask task = new SecretScanTask(currentRepoId);
        task.setOnSucceeded(e -> {
            List<SecretScanResultDTO> findings = task.getValue();
            secretsTable.getItems().setAll(findings);
            if (findings.isEmpty()) {
                scannerBanner.setText("✅ Clean! No leaked secrets or credentials detected.");
                scannerBanner.setStyle("-fx-text-fill: #4ADE80; -fx-font-weight: bold;");
                AlertHelper.showSuccess("Scanner Clean", "No hardcoded credentials found in this repository.");
            } else {
                scannerBanner.setText("🚨 ALERT: " + findings.size() + " hardcoded secrets detected in repository!");
                scannerBanner.setStyle("-fx-text-fill: #F87171; -fx-font-weight: bold;");
                AlertHelper.showError("Secrets Detected", "Found " + findings.size() + " secret leaks. Review the findings table.");
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            scannerBanner.setText("Scan failed: " + extractMessage(ex));
            scannerBanner.setStyle("-fx-text-fill: #F87171;");
        });

        ThreadPools.COMPUTE_POOL.submit(task);
    }

    @FXML
    private void handleBackToDashboard() {
        FxmlNavigator.navigateTo("dashboard.fxml");
    }

    @FXML
    public void loadComments() {
        if (currentRepoId == null) return;
        commentsContainer.getChildren().clear();
        commentsContainer.getChildren().add(new Label("Loading comments..."));

        commentApi.getCommentsForRepo(currentRepoId).whenComplete((comments, error) -> {
            Platform.runLater(() -> {
                commentsContainer.getChildren().clear();
                if (error != null) {
                    commentsContainer.getChildren().add(new Label("Failed to load comments: " + extractMessage(error)));
                } else if (comments == null || comments.isEmpty()) {
                    commentsContainer.getChildren().add(new Label("No comments or suggestions yet. Be the first to post!"));
                } else {
                    renderCommentsTree(comments, commentsContainer, 0);
                }
            });
        });
    }

    private void renderCommentsTree(List<com.forknight.common.dto.comment.CommentDTO> comments, VBox container, int depth) {
        for (com.forknight.common.dto.comment.CommentDTO c : comments) {
            VBox card = new VBox(5);
            card.getStyleClass().add("card");
            card.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
            if (depth > 0) {
                card.setStyle("-fx-background-color: #151827; -fx-border-left-color: #7C3AED; -fx-border-left-width: 3;");
                VBox.setMargin(card, new javafx.geometry.Insets(0, 0, 0, depth * 25));
            }

            javafx.scene.layout.HBox authorRow = new javafx.scene.layout.HBox(8);
            authorRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label authorLbl = new Label("@" + c.authorUsername());
            authorLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #F1F3FA;");

            Label dateLbl = new Label(c.createdAt() != null ? c.createdAt().toString() : "");
            dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #666B82;");

            authorRow.getChildren().addAll(authorLbl, dateLbl);

            if (c.isSuggestion()) {
                Label suggBadge = new Label("💡 Suggestion");
                suggBadge.getStyleClass().add("status-badge-pending");
                authorRow.getChildren().add(suggBadge);
            }

            if (c.filePath() != null && !c.filePath().isBlank()) {
                Label fileBadge = new Label("📄 " + c.filePath() + (c.lineNumber() != null ? ":" + c.lineNumber() : ""));
                fileBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #9A9FB5;");
                authorRow.getChildren().add(fileBadge);
            }

            Label contentLbl = new Label(c.content());
            contentLbl.setWrapText(true);
            contentLbl.setStyle("-fx-text-fill: #F1F3FA; -fx-padding: 4 0;");

            Button replyBtn = new Button("Reply");
            replyBtn.getStyleClass().add("button-secondary");
            replyBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2 8;");
            TooltipHelper.install(replyBtn, "Write a reply to @" + c.authorUsername() + "'s comment");
            replyBtn.setOnAction(e -> handleReply(c));

            card.getChildren().addAll(authorRow, contentLbl, replyBtn);
            container.getChildren().add(card);

            if (c.replies() != null && !c.replies().isEmpty()) {
                renderCommentsTree(c.replies(), container, depth + 1);
            }
        }
    }

    private void handleReply(com.forknight.common.dto.comment.CommentDTO parentComment) {
        TextInputDialog replyDialog = new TextInputDialog();
        replyDialog.setTitle("Reply to Comment");
        replyDialog.setHeaderText("Replying to @" + parentComment.authorUsername());
        replyDialog.setContentText("Your reply:");

        replyDialog.showAndWait().ifPresent(replyText -> {
            if (replyText.trim().isEmpty()) return;
            com.forknight.common.dto.comment.CommentCreateRequest req = 
                    new com.forknight.common.dto.comment.CommentCreateRequest(
                            currentRepoId,
                            parentComment.id(),
                            replyText.trim(),
                            false,
                            null,
                            null
                    );
            commentApi.postComment(req).whenComplete((res, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Reply Failed", extractMessage(err));
                    } else {
                        loadComments();
                    }
                });
            });
        });
    }

    @FXML
    private void handlePostComment() {
        if (currentRepoId == null) return;
        String content = newCommentArea.getText();
        if (content == null || content.trim().isEmpty()) {
            AlertHelper.showError("Required", "Comment text cannot be empty.");
            return;
        }

        String filePath = selectedFileNode != null ? selectedFileNode.path() : null;
        boolean isSuggestion = isSuggestionCheck.isSelected();

        com.forknight.common.dto.comment.CommentCreateRequest request = 
                new com.forknight.common.dto.comment.CommentCreateRequest(
                        currentRepoId,
                        null,
                        content.trim(),
                        isSuggestion,
                        filePath,
                        null
                );

        commentApi.postComment(request).whenComplete((comment, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    AlertHelper.showError("Failed to Post", extractMessage(error));
                } else {
                    newCommentArea.clear();
                    isSuggestionCheck.setSelected(false);
                    loadComments();
                }
            });
        });
    }

    @FXML
    private void handleHeaderRequestContrib() {
        if (currentRepo == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Request Contribution Access");
        dialog.setHeaderText("Request to contribute to " + currentRepo.name());
        dialog.setContentText("Introduce yourself and what you'd like to work on:");

        dialog.showAndWait().ifPresent(msg -> {
            if (msg.trim().isEmpty()) return;
            com.forknight.common.dto.contrib.ContribCreateRequest req = 
                    new com.forknight.common.dto.contrib.ContribCreateRequest(currentRepoId, msg.trim());

            contribApi.requestContribution(req).whenComplete((res, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Request Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Request Sent", "Contribution request sent to @" + currentRepo.ownerUsername());
                    }
                });
            });
        });
    }

    @FXML
    private void handleHeaderRequestCopy() {
        if (currentRepo == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Request Copy / License");
        dialog.setHeaderText("Request permission to copy / fork " + currentRepo.name());
        dialog.setContentText("Describe intended usage (Open Source / Commercial):");

        dialog.showAndWait().ifPresent(msg -> {
            if (msg.trim().isEmpty()) return;
            com.forknight.common.dto.licensing.CopyLicenseCreateRequest req = 
                    new com.forknight.common.dto.licensing.CopyLicenseCreateRequest(currentRepoId, msg.trim());

            copyLicenseApi.requestCopy(req).whenComplete((res, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Request Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Request Sent", "Copy/license request submitted to @" + currentRepo.ownerUsername());
                    }
                });
            });
        });
    }

    private void setup3DGraph() {
        gitGraph3DView = new com.forknight.client.component.GitGraph3DView();
        gitGraph3DView.setOnCommitSelected(node -> {
            Platform.runLater(() -> {
                inspectorHashLabel.setText(node.hash() != null ? node.hash() : "-");
                inspectorBranchLabel.setText(node.branchName() != null ? node.branchName() : "-");
                inspectorAuthorLabel.setText(node.authorName() != null ? node.authorName() + " <" + node.authorEmail() + ">" : "-");
                inspectorDateLabel.setText(node.timestamp() != null ? node.timestamp().toString() : "-");
                inspectorMessageArea.setText(node.message() != null ? node.message() : "");
            });
        });
        graph3DContainer.getChildren().add(gitGraph3DView);
    }

    @FXML
    public void loadGraph3D() {
        if (currentRepoId == null) return;
        repoApi.getGraph3D(currentRepoId).whenComplete((graphData, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    System.err.println("Could not load 3D graph: " + extractMessage(error));
                } else if (graphData != null) {
                    gitGraph3DView.renderGraph(graphData);
                }
            });
        });
    }

    @FXML
    public void handleReloadGraph() {
        if (is3DMode) {
            loadGraph3D();
        } else {
            loadGraph2D();
        }
    }

    @FXML
    public void handleResetGraphView() {
        if (!is3DMode && graph2DScroll != null) {
            graph2DScroll.setHvalue(0);
            graph2DScroll.setVvalue(0);
        }
        // If 3D mode reset is needed, it would be handled in GitGraph3DView
    }

    @FXML
    public void handleGraphToggle() {
        is3DMode = graphToggleBtn.isSelected();
        if (is3DMode) {
            // Switch to 3D
            graphModeLabel.setText("3D Commit Graph  (Auto-rotating)");
            graphToggleBtn.setText("Switch to 2D");
            graph2DScroll.setVisible(false);
            graph3DContainer.setVisible(true);
            loadGraph3D();
        } else {
            // Switch to 2D
            graphModeLabel.setText("2D Commit Timeline");
            graphToggleBtn.setText("Switch to 3D");
            graph3DContainer.setVisible(false);
            graph2DScroll.setVisible(true);
            loadGraph2D();
        }
    }

    private void loadGraph2D() {
        if (currentRepoId == null) return;
        repoApi.getGraph3D(currentRepoId).whenComplete((graphData, error) -> {
            Platform.runLater(() -> {
                if (error != null || graphData == null) return;
                draw2DGraph(graphData);
            });
        });
    }

    private void draw2DGraph(com.forknight.common.dto.graph.GitGraphDataDTO graphData) {
        if (graphData == null || graphData.nodes() == null || graphData.nodes().isEmpty()) return;

        java.util.List<com.forknight.common.dto.graph.CommitNode3D> nodes = graphData.nodes();
        int nodeCount = nodes.size();

        double padding = 40;
        double nodeRadius = 10;
        double rowHeight = 60;
        double canvasHeight = Math.max(600, nodeCount * rowHeight + padding * 2);
        double canvasWidth = Math.max(800, graph2DScroll.getWidth() > 0 ? graph2DScroll.getWidth() - 20 : 800);

        graph2DCanvas.setWidth(canvasWidth);
        graph2DCanvas.setHeight(canvasHeight);

        javafx.scene.canvas.GraphicsContext gc = graph2DCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        // Dark background
        gc.setFill(javafx.scene.paint.Color.web("#0B0C14"));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Build node -> index map for edge drawing
        java.util.Map<String, Integer> nodeIndex = new java.util.HashMap<>();
        for (int i = 0; i < nodeCount; i++) {
            if (nodes.get(i).hash() != null) nodeIndex.put(nodes.get(i).hash(), i);
        }

        double lineX = padding + nodeRadius;

        // Draw edges first
        gc.setStroke(javafx.scene.paint.Color.web("#3B3F5C"));
        gc.setLineWidth(2);
        if (graphData.edges() != null) {
            for (com.forknight.common.dto.graph.Edge3D edge : graphData.edges()) {
                Integer fromIdx = nodeIndex.get(edge.fromHash());
                Integer toIdx = nodeIndex.get(edge.toHash());
                if (fromIdx != null && toIdx != null) {
                    double y1 = padding + fromIdx * rowHeight + nodeRadius;
                    double y2 = padding + toIdx * rowHeight + nodeRadius;
                    gc.strokeLine(lineX, y1, lineX, y2);
                }
            }
        }

        // Draw nodes and labels
        for (int i = 0; i < nodeCount; i++) {
            com.forknight.common.dto.graph.CommitNode3D node = nodes.get(i);
            double cy = padding + i * rowHeight + nodeRadius;

            // Use the node's own color if available, otherwise purple
            String colorStr = (node.colorHex() != null && !node.colorHex().isEmpty()) ? node.colorHex() : "#8B5CF6";

            // Glow circle
            gc.setFill(javafx.scene.paint.Color.web(colorStr, 0.25));
            gc.fillOval(lineX - nodeRadius - 4, cy - nodeRadius - 4, (nodeRadius + 4) * 2, (nodeRadius + 4) * 2);

            // Main node dot
            gc.setFill(javafx.scene.paint.Color.web(colorStr));
            gc.fillOval(lineX - nodeRadius, cy - nodeRadius, nodeRadius * 2, nodeRadius * 2);

            // Branch badge
            double textX = lineX + nodeRadius + 15;
            if (node.branchName() != null && !node.branchName().isEmpty()) {
                gc.setFill(javafx.scene.paint.Color.web("#1C1F30"));
                gc.fillRoundRect(textX - 4, cy - 10, node.branchName().length() * 7.5 + 8, 20, 8, 8);
                gc.setFill(javafx.scene.paint.Color.web(colorStr));
                gc.setFont(javafx.scene.text.Font.font("Monospace", javafx.scene.text.FontWeight.BOLD, 11));
                gc.fillText(node.branchName(), textX, cy + 4);
                textX += node.branchName().length() * 7.5 + 20;
            }

            // Commit message
            String msg = node.message() != null ? node.message() : "(no message)";
            if (msg.length() > 60) msg = msg.substring(0, 57) + "...";
            gc.setFill(javafx.scene.paint.Color.web("#F1F3FA"));
            gc.setFont(javafx.scene.text.Font.font("System", 13));
            gc.fillText(msg, textX, cy + 4);

            // Short hash at right
            String shortH = node.shortHash() != null ? node.shortHash() : (node.hash() != null && node.hash().length() >= 7 ? node.hash().substring(0, 7) : "");
            if (!shortH.isEmpty()) {
                gc.setFill(javafx.scene.paint.Color.web("#9A9FB5"));
                gc.setFont(javafx.scene.text.Font.font("Monospace", 11));
                gc.fillText(shortH, canvasWidth - 90, cy + 4);
            }
        }

        // Click handler to populate inspector
        graph2DCanvas.setOnMouseClicked(evt -> {
            int clickedIdx = (int) ((evt.getY() - padding) / rowHeight);
            if (clickedIdx >= 0 && clickedIdx < nodeCount) {
                com.forknight.common.dto.graph.CommitNode3D clicked = nodes.get(clickedIdx);
                inspectorHashLabel.setText(clicked.hash() != null ? clicked.hash() : "-");
                inspectorBranchLabel.setText(clicked.branchName() != null ? clicked.branchName() : "-");
                inspectorAuthorLabel.setText(clicked.authorName() != null ? clicked.authorName() : "-");
                inspectorDateLabel.setText(clicked.timestamp() != null ? clicked.timestamp().toString() : "-");
                inspectorMessageArea.setText(clicked.message() != null ? clicked.message() : "-");
            }
        });
    }


    // ==========================================
    // Pull Requests & Interactive Code Reviews
    // ==========================================

    @FXML
    public void loadPullRequests() {
        if (currentRepoId == null || prContainer == null) return;
        prApi.getRepoPRs(currentRepoId).whenComplete((prs, error) -> {
            Platform.runLater(() -> {
                prContainer.getChildren().clear();
                if (error != null) {
                    Label errLabel = new Label("Could not load PRs: " + extractMessage(error));
                    errLabel.setStyle("-fx-text-fill: #F87171;");
                    prContainer.getChildren().add(errLabel);
                } else if (prs == null || prs.isEmpty()) {
                    Label empty = new Label("No pull requests open. Click '+ New Pull Request' to create one.");
                    empty.setStyle("-fx-text-fill: #666B82; -fx-font-style: italic; -fx-padding: 15px;");
                    prContainer.getChildren().add(empty);
                } else {
                    for (PullRequestDTO pr : prs) {
                        HBox card = new HBox(15);
                        card.getStyleClass().add("repo-card");
                        card.setAlignment(Pos.CENTER_LEFT);
                        card.setPadding(new Insets(12, 15, 12, 15));

                        VBox info = new VBox(4);
                        HBox.setHgrow(info, Priority.ALWAYS);

                        HBox titleRow = new HBox(10);
                        titleRow.setAlignment(Pos.CENTER_LEFT);

                        Label titleLabel = new Label("#" + pr.id() + " " + pr.title());
                        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #F1F3FA;");

                        Label statusBadge = new Label(pr.status().name());
                        statusBadge.getStyleClass().add(pr.status() == PRStatus.MERGED ? "status-badge-approved" :
                                (pr.status() == PRStatus.OPEN ? "status-badge-pending" : "badge"));
                        TooltipHelper.install(statusBadge, "Current status of this pull request: " + pr.status().name());

                        Label branchBadge = new Label(pr.sourceBranch() + " → " + pr.targetBranch());
                        branchBadge.setStyle("-fx-background-color: rgba(56,189,248,0.12); -fx-text-fill: #38BDF8; -fx-font-size: 11px; -fx-padding: 2 6; -fx-background-radius: 4px; -fx-border-color: rgba(56,189,248,0.25); -fx-border-radius: 4px; -fx-border-width: 1;");
                        TooltipHelper.install(branchBadge, "Proposing to merge code from '" + pr.sourceBranch() + "' into '" + pr.targetBranch() + "'");

                        titleRow.getChildren().addAll(titleLabel, statusBadge, branchBadge);

                        Label metaLabel = new Label(String.format("Opened by @%s • %d file(s) changed (+%d / -%d)",
                                pr.authorUsername(), pr.filesChanged(), pr.additions(), pr.deletions()));
                        metaLabel.setStyle("-fx-text-fill: #666B82; -fx-font-size: 11px;");
                        TooltipHelper.install(metaLabel, "Pull request author and lines of code changed");

                        info.getChildren().addAll(titleRow, metaLabel);

                        Button viewBtn = new Button("Inspect Diff & AI Review →");
                        viewBtn.getStyleClass().add("button-secondary");
                        TooltipHelper.install(viewBtn, "Open this pull request to inspect the code diffs and request an AI review");
                        viewBtn.setOnAction(e -> showPrDetailModal(pr));

                        card.getChildren().addAll(info, viewBtn);
                        prContainer.getChildren().add(card);
                    }
                }
            });
        });
    }

    @FXML
    private void handleNewPullRequest() {
        if (currentRepoId == null) return;

        Dialog<PRCreateRequest> dialog = new Dialog<>();
        dialog.setTitle("New Pull Request");
        dialog.setHeaderText("Open a Pull Request on " + (currentRepo != null ? currentRepo.name() : ""));

        DialogPane pane = dialog.getDialogPane();
        ButtonType createBtn = new ButtonType("Create Pull Request", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(createBtn, ButtonType.CANCEL);
        try {
            pane.getStylesheets().addAll(
                    getClass().getResource("/css/theme-dark.css").toExternalForm(),
                    getClass().getResource("/css/components.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField titleField = new TextField();
        titleField.setPromptText("Title (e.g. Implement fast matrix multiplication)");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Explain the purpose and changes in this PR...");
        descArea.setPrefRowCount(3);

        TextField sourceBranchField = new TextField("feature-branch");
        TextField targetBranchField = new TextField("main");

        grid.add(new Label("PR Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Source Branch:"), 0, 2);
        grid.add(sourceBranchField, 1, 2);
        grid.add(new Label("Target Branch:"), 0, 3);
        grid.add(targetBranchField, 1, 3);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) {
                if (titleField.getText().isBlank()) return null;
                return new PRCreateRequest(
                        currentRepoId,
                        titleField.getText().trim(),
                        descArea.getText().trim(),
                        sourceBranchField.getText().trim(),
                        targetBranchField.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(req -> {
            prApi.createPR(currentRepoId, req).whenComplete((created, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Creation Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("PR Created", "Pull request #" + created.id() + " opened successfully!");
                        loadPullRequests();
                    }
                });
            });
        });
    }

    private void showPrDetailModal(PullRequestDTO pr) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("PR #" + pr.id() + ": " + pr.title());
        dialog.setHeaderText("Pull Request Review: " + pr.sourceBranch() + " → " + pr.targetBranch());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        try {
            pane.getStylesheets().addAll(
                    getClass().getResource("/css/theme-dark.css").toExternalForm(),
                    getClass().getResource("/css/components.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        TabPane prTabPane = new TabPane();
        prTabPane.setPrefSize(780, 520);

        // Tab 1: Diff Viewer
        Tab diffTab = new Tab("🔍 Code Diff");
        diffTab.setClosable(false);
        DiffViewerComponent diffViewer = new DiffViewerComponent();
        diffTab.setContent(diffViewer);

        // Fetch PR Diffs
        prApi.getPRDiff(pr.id()).whenComplete((diffs, err) -> {
            Platform.runLater(() -> {
                if (diffs != null) diffViewer.setDiffs(diffs);
            });
        });

        // Tab 2: AI Review
        Tab aiTab = new Tab("🤖 AI Code Review");
        aiTab.setClosable(false);
        VBox aiBox = new VBox(10);
        aiBox.setPadding(new Insets(10));

        HBox aiActions = new HBox(10);
        aiActions.setAlignment(Pos.CENTER_LEFT);
        Button runAiBtn = new Button("⚡ Re-Run AI Review");
        runAiBtn.getStyleClass().add("button-secondary");
        TooltipHelper.install(runAiBtn, "Run the AI Code Reviewer on this pull request again");
        Label aiStatusLabel = new Label("AI insights automatically evaluated on PR creation.");
        aiStatusLabel.setStyle("-fx-text-fill: #9A9FB5; -fx-font-size: 11px;");
        aiActions.getChildren().addAll(runAiBtn, aiStatusLabel);

        VBox reviewList = new VBox(10);
        ScrollPane aiScroll = new ScrollPane(reviewList);
        aiScroll.setFitToWidth(true);
        VBox.setVgrow(aiScroll, Priority.ALWAYS);

        aiBox.getChildren().addAll(aiActions, aiScroll);
        aiTab.setContent(aiBox);

        // Load existing reviews
        prApi.getPrReviews(pr.id()).whenComplete((reviews, err) -> {
            Platform.runLater(() -> {
                if (reviews != null && !reviews.isEmpty()) {
                    reviewList.getChildren().clear();
                    for (AiReviewDTO r : reviews) {
                        reviewList.getChildren().add(new AiReviewCardComponent(r));
                    }
                }
            });
        });

        runAiBtn.setOnAction(e -> {
            runAiBtn.setDisable(true);
            prApi.triggerAiReview(pr.id(), "intelligent-reviewer-v2", null).whenComplete((rev, err) -> {
                Platform.runLater(() -> {
                    runAiBtn.setDisable(false);
                    if (err != null) {
                        AlertHelper.showError("AI Review Failed", extractMessage(err));
                    } else if (rev != null) {
                        reviewList.getChildren().add(0, new AiReviewCardComponent(rev));
                    }
                });
            });
        });

        prTabPane.getTabs().addAll(diffTab, aiTab);

        VBox modalContent = new VBox(10);
        modalContent.getChildren().add(prTabPane);

        if (pr.status() == PRStatus.OPEN) {
            HBox mergeBar = new HBox(10);
            mergeBar.setAlignment(Pos.CENTER_RIGHT);
            mergeBar.setPadding(new Insets(5, 10, 5, 10));

            Button mergeBtn = new Button("✔ Merge Pull Request");
            mergeBtn.getStyleClass().add("button");
            TooltipHelper.install(mergeBtn, "Merge these changes into the target branch permanently");
            mergeBtn.setOnAction(e -> {
                mergeBtn.setDisable(true);
                prApi.mergePR(pr.id()).whenComplete((res, err) -> {
                    Platform.runLater(() -> {
                        if (err != null) {
                            AlertHelper.showError("Merge Failed", extractMessage(err));
                            mergeBtn.setDisable(false);
                        } else {
                            AlertHelper.showSuccess("PR Merged", "Pull request #" + pr.id() + " merged successfully!");
                            dialog.close();
                            loadPullRequests();
                            loadCommits();
                        }
                    });
                });
            });

            mergeBar.getChildren().add(mergeBtn);
            modalContent.getChildren().add(mergeBar);
        }

        pane.setContent(modalContent);
        dialog.showAndWait();
    }

    // ==========================================
    // Standalone AI Code Reviewer
    // ==========================================

    @FXML
    private void handleReviewCurrentFile() {
        if (codeViewer != null && !codeViewer.getText().isBlank()) {
            aiCodeInputArea.setText(codeViewer.getText());
        }
    }

    @FXML
    private void handleRunStandaloneAiReview() {
        if (aiCodeInputArea == null || aiCodeInputArea.getText().isBlank()) {
            AlertHelper.showWarning("Input Required", "Please paste or load code in the input area before running AI review.");
            return;
        }

        aiLoadingSpinner.setVisible(true);
        String code = aiCodeInputArea.getText();
        String model = aiModelCombo != null ? aiModelCombo.getValue() : "intelligent-reviewer-v2";
        String apiKey = aiApiKeyField != null ? aiApiKeyField.getText().trim() : null;

        AiReviewRequest request = new AiReviewRequest(null, code, "java", model, apiKey);

        aiReviewApi.reviewCode(request).whenComplete((review, error) -> {
            Platform.runLater(() -> {
                aiLoadingSpinner.setVisible(false);
                if (error != null) {
                    AlertHelper.showError("Review Failed", extractMessage(error));
                } else if (review != null) {
                    aiReviewContainer.getChildren().clear();
                    aiReviewContainer.getChildren().add(new AiReviewCardComponent(review));
                }
            });
        });
    }

    // ==========================================
    // Git Command Palette
    // ==========================================

    @FXML
    private void handleOpenGitPalette() {
        if (currentRepoId == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Git Command Palette");
        dialog.setHeaderText("⚡ Git Operations — " + (currentRepo != null ? currentRepo.name() : ""));

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        try {
            pane.getStylesheets().addAll(
                    getClass().getResource("/css/theme-dark.css").toExternalForm(),
                    getClass().getResource("/css/components.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        VBox contentBox = new VBox(15);
        contentBox.setPrefWidth(550);
        contentBox.setPadding(new Insets(15));

        // 1. Status Bar
        HBox statusRow = new HBox(12);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Label branchTitle = new Label("Current Branch:");
        branchTitle.setStyle("-fx-text-fill: #9A9FB5; -fx-font-weight: bold;");
        Label branchBadge = new Label("main");
        branchBadge.getStyleClass().add("badge");
        Label statusText = new Label("Checking status...");
        statusText.setStyle("-fx-text-fill: #666B82; -fx-font-style: italic;");
        statusRow.getChildren().addAll(branchTitle, branchBadge, statusText);

        // 2. Commit Section
        VBox commitSection = new VBox(8);
        commitSection.getStyleClass().add("card");
        Label commitSectionTitle = new Label("Commit Working Tree Changes");
        commitSectionTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #F1F3FA;");
        TextField commitMsgField = new TextField();
        commitMsgField.setPromptText("Commit message (e.g. refactor: optimize tree traversal)");
        HBox commitBtnRow = new HBox(10);
        commitBtnRow.setAlignment(Pos.CENTER_RIGHT);
        Button commitBtn = new Button("✔ Commit All Changes");
        commitBtn.getStyleClass().add("button");
        Button stashBtn = new Button("📦 Stash Changes");
        stashBtn.getStyleClass().add("button-secondary");
        commitBtnRow.getChildren().addAll(stashBtn, commitBtn);
        commitSection.getChildren().addAll(commitSectionTitle, commitMsgField, commitBtnRow);

        // 3. Branching Section
        VBox branchSection = new VBox(8);
        branchSection.getStyleClass().add("card");
        Label branchSectionTitle = new Label("Branch Management");
        branchSectionTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #F1F3FA;");

        HBox switchRow = new HBox(10);
        switchRow.setAlignment(Pos.CENTER_LEFT);
        Label switchLabel = new Label("Switch to:");
        ComboBox<String> branchCombo = new ComboBox<>();
        branchCombo.setPrefWidth(180);
        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.getStyleClass().add("button-secondary");
        switchRow.getChildren().addAll(switchLabel, branchCombo, checkoutBtn);

        HBox newBranchRow = new HBox(10);
        newBranchRow.setAlignment(Pos.CENTER_LEFT);
        TextField newBranchField = new TextField();
        newBranchField.setPromptText("New branch name (e.g. feature/auth-v2)");
        Button createBranchBtn = new Button("+ Create Branch");
        createBranchBtn.getStyleClass().add("button-secondary");
        newBranchRow.getChildren().addAll(newBranchField, createBranchBtn);

        branchSection.getChildren().addAll(branchSectionTitle, switchRow, newBranchRow);

        contentBox.getChildren().addAll(statusRow, commitSection, branchSection);
        pane.setContent(contentBox);

        // Load Live Git Status
        Runnable refreshStatus = () -> {
            repoApi.getGitStatus(currentRepoId).whenComplete((status, err) -> {
                Platform.runLater(() -> {
                    if (status != null) {
                        branchBadge.setText(status.currentBranch());
                        statusText.setText(status.isClean() ? "Working tree clean ✓" :
                                (status.modifiedFiles().size() + status.untrackedFiles().size()) + " files modified");
                        statusText.setStyle(status.isClean() ? "-fx-text-fill: #4ADE80;" : "-fx-text-fill: #FBBF24;");
                        branchCombo.getItems().setAll(status.branches());
                        branchCombo.setValue(status.currentBranch());
                    }
                });
            });
        };

        refreshStatus.run();

        // Wire Actions
        commitBtn.setOnAction(e -> {
            String msg = commitMsgField.getText().trim();
            if (msg.isEmpty()) {
                AlertHelper.showWarning("Input Required", "Please enter a commit message.");
                return;
            }
            commitBtn.setDisable(true);
            repoApi.commitChanges(currentRepoId, msg).whenComplete((c, err) -> {
                Platform.runLater(() -> {
                    commitBtn.setDisable(false);
                    if (err != null) {
                        AlertHelper.showError("Commit Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Committed", "Committed [" + c.shortHash() + "]: " + c.message());
                        commitMsgField.clear();
                        refreshStatus.run();
                        loadCommits();
                        loadGraph2D();
                    }
                });
            });
        });

        stashBtn.setOnAction(e -> {
            repoApi.stashChanges(currentRepoId).whenComplete((res, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Stash Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Stashed", "Working tree changes stashed successfully.");
                        refreshStatus.run();
                    }
                });
            });
        });

        checkoutBtn.setOnAction(e -> {
            String chosen = branchCombo.getValue();
            if (chosen != null && !chosen.isBlank()) {
                repoApi.checkoutBranch(currentRepoId, chosen).whenComplete((s, err) -> {
                    Platform.runLater(() -> {
                        if (err != null) {
                            AlertHelper.showError("Checkout Failed", extractMessage(err));
                        } else {
                            AlertHelper.showSuccess("Switched Branch", "Now on branch: " + chosen);
                            refreshStatus.run();
                            refreshFileTree();
                            loadCommits();
                            loadGraph2D();
                        }
                    });
                });
            }
        });

        createBranchBtn.setOnAction(e -> {
            String newBranch = newBranchField.getText().trim();
            if (newBranch.isEmpty()) return;
            repoApi.createBranch(currentRepoId, newBranch).whenComplete((s, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Branch Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Branch Created", "Created branch: " + newBranch);
                        newBranchField.clear();
                        refreshStatus.run();
                    }
                });
            });
        });

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

