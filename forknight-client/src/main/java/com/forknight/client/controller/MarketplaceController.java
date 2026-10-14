package com.forknight.client.controller;

import com.forknight.client.api.MarketplaceApi;
import com.forknight.client.api.RepoApi;
import com.forknight.client.api.ContribApi;
import com.forknight.common.dto.repo.RepoDTO;
import com.forknight.common.dto.contrib.ContribCreateRequest;
import com.forknight.client.util.AlertHelper;
import com.forknight.client.util.TooltipHelper;
import com.forknight.common.dto.market.RatingCreateRequest;
import com.forknight.common.dto.market.SnippetCreateRequest;
import com.forknight.common.dto.market.SnippetDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MarketplaceController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> languageFilterCombo;
    @FXML private ComboBox<String> priceFilterCombo;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private VBox snippetsContainer;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private final MarketplaceApi marketApi = new MarketplaceApi();
    private final RepoApi repoApi = new RepoApi();
    private final ContribApi contribApi = new ContribApi();
    @FXML private VBox projectsContainer;
    private int currentPage = 0;
    private final int pageSize = 10;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        languageFilterCombo.getItems().addAll("All", "Java", "Python", "JavaScript", "TypeScript", "Rust", "Go", "C++", "SQL");
        languageFilterCombo.setValue("All");

        priceFilterCombo.getItems().addAll("All", "Free ($0)", "Under $10", "Under $50");
        priceFilterCombo.setValue("All");

        loadSnippets();
        loadProjects();
    }

    @FXML
    public void handleSearch() {
        currentPage = 0;
        loadSnippets();
        loadProjects();
    }

    @FXML
    public void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadSnippets();
        loadProjects();
        }
    }

    @FXML
    public void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadSnippets();
        loadProjects();
        }
    }

    @FXML
    public void loadSnippets() {
        loadingSpinner.setVisible(true);
        statusLabel.setText("Browsing marketplace...");

        String lang = languageFilterCombo.getValue();
        if ("All".equalsIgnoreCase(lang)) lang = null;

        BigDecimal maxPrice = null;
        String priceChoice = priceFilterCombo.getValue();
        if ("Free ($0)".equals(priceChoice)) maxPrice = BigDecimal.ZERO;
        else if ("Under $10".equals(priceChoice)) maxPrice = new BigDecimal("10.00");
        else if ("Under $50".equals(priceChoice)) maxPrice = new BigDecimal("50.00");

        String query = searchField.getText();

        marketApi.searchSnippets(lang, maxPrice, query, currentPage, pageSize).whenComplete((response, error) -> {
            Platform.runLater(() -> {
                loadingSpinner.setVisible(false);
                if (error != null) {
                    statusLabel.setText("Failed to load snippets: " + extractMessage(error));
                } else if (response == null || response.content().isEmpty()) {
                    statusLabel.setText("No code snippets found matching criteria.");
                    snippetsContainer.getChildren().clear();
                } else {
                    totalPages = Math.max(1, response.totalPages());
                    pageInfoLabel.setText(String.format("Page %d of %d (%d items)",
                            currentPage + 1, totalPages, response.totalElements()));
                    prevPageButton.setDisable(currentPage <= 0);
                    nextPageButton.setDisable(currentPage >= totalPages - 1);

                    statusLabel.setText("Showing " + response.totalElements() + " marketplace snippets");
                    renderSnippetCards(response.content());
                }
            });
        });
    }

    private void renderSnippetCards(List<SnippetDTO> snippets) {
        snippetsContainer.getChildren().clear();
        for (SnippetDTO s : snippets) {
            HBox card = new HBox(15);
            card.getStyleClass().add("repo-card");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(15));

            VBox infoBox = new VBox(5);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            HBox titleRow = new HBox(10);
            titleRow.setAlignment(Pos.CENTER_LEFT);

            Label titleLabel = new Label("💡 " + s.title());
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

            Label langBadge = new Label(s.language().toUpperCase());
            langBadge.getStyleClass().add("badge");
            TooltipHelper.install(langBadge, "Programming language: " + s.language());

            String priceText = s.price().compareTo(BigDecimal.ZERO) == 0 ? "FREE" : "$" + s.price();
            Label priceBadge = new Label(priceText);
            priceBadge.getStyleClass().add(s.price().compareTo(BigDecimal.ZERO) == 0 ? "status-badge-approved" : "status-badge-pending");
            TooltipHelper.install(priceBadge, s.price().compareTo(BigDecimal.ZERO) == 0 ? "This snippet is free to download" : "This snippet costs $" + s.price() + " to purchase");

            titleRow.getChildren().addAll(titleLabel, langBadge, priceBadge);

            Label descLabel = new Label(s.description() != null ? s.description() : "No description provided.");
            descLabel.getStyleClass().add("label-subtitle");
            descLabel.setWrapText(true);

            Label statsLabel = new Label(String.format("⭐ %.1f (%d reviews) | 🛒 %d sales | by @%s",
                    s.ratingAvg(), s.ratingCount(), s.salesCount(), s.authorUsername()));
            statsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            TooltipHelper.install(statsLabel, "Average rating, total sales, and author information");

            infoBox.getChildren().addAll(titleRow, descLabel, statsLabel);

            Button inspectBtn = new Button(s.isPurchasedByCurrentUser() ? "View Code ✓" : "Inspect / Buy →");
            inspectBtn.getStyleClass().add(s.isPurchasedByCurrentUser() ? "button-secondary" : "button");
            TooltipHelper.install(inspectBtn, s.isPurchasedByCurrentUser() ? "View the code for this purchased snippet" : "View details, reviews, and purchase options for this snippet");
            inspectBtn.setOnAction(e -> showSnippetDetailsModal(s));

            card.getChildren().addAll(infoBox, inspectBtn);
            snippetsContainer.getChildren().add(card);
        }
    }

        private void loadProjects() {
        repoApi.exploreRepos(searchField.getText(), currentPage, pageSize).whenComplete((response, error) -> {
            Platform.runLater(() -> {
                if (error == null && response != null) {
                    renderProjectCards(response.content());
                }
            });
        });
    }

    private void renderProjectCards(List<RepoDTO> repos) {
        if (projectsContainer == null) return;
        projectsContainer.getChildren().clear();
        for (RepoDTO repo : repos) {
            HBox card = new HBox(15);
            card.getStyleClass().add("repo-card");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(15));

            VBox infoBox = new VBox(5);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            Label titleLabel = new Label(repo.name());
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");
            
            Label ownerLabel = new Label("Owner: " + repo.ownerUsername());
            ownerLabel.setStyle("-fx-text-fill: #94a3b8;");

            Label descLabel = new Label(repo.description() != null ? repo.description() : "No description");
            descLabel.getStyleClass().add("label-subtitle");

            infoBox.getChildren().addAll(titleLabel, ownerLabel, descLabel);

            Button requestBtn = new Button("Request Contribution");
            requestBtn.getStyleClass().add("button");
            TooltipHelper.install(requestBtn, "Send a contribution request to the owner. If accepted, you can contribute or add files.");
            requestBtn.setOnAction(e -> handleRequestContribution(repo));

            card.getChildren().addAll(infoBox, requestBtn);
            projectsContainer.getChildren().add(card);
        }
    }

    private void handleRequestContribution(RepoDTO repo) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Contribution Request");
        dialog.setHeaderText("Request to contribute to " + repo.name());
        
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Why do you want to contribute? e.g. 'I'd like to add some new features.'");
        messageArea.setWrapText(true);
        pane.setContent(messageArea);
        
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? messageArea.getText() : null);
        
        dialog.showAndWait().ifPresent(msg -> {
            contribApi.requestContribution(new ContribCreateRequest(repo.id(), msg)).whenComplete((res, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Request Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Request Sent", "Contribution request sent to " + repo.ownerUsername() + "!");
                    }
                });
            });
        });
    }

    private void showSnippetDetailsModal(SnippetDTO snippet) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(snippet.title());
        dialog.setHeaderText("Code Snippet Details");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        try {
            pane.getStylesheets().addAll(
                    getClass().getResource("/css/theme-dark.css").toExternalForm(),
                    getClass().getResource("/css/components.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(10));
        contentBox.setPrefWidth(650);
        contentBox.setPrefHeight(480);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(snippet.title());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");
        String priceText = snippet.price().compareTo(BigDecimal.ZERO) == 0 ? "FREE" : "$" + snippet.price();
        Label price = new Label(priceText);
        price.getStyleClass().add("badge");
        header.getChildren().addAll(title, price);

        Label desc = new Label(snippet.description());
        desc.getStyleClass().add("label-subtitle");
        desc.setWrapText(true);

        TextArea codeArea = new TextArea(snippet.codeContent());
        codeArea.setEditable(false);
        codeArea.getStyleClass().add("code-view");
        codeArea.setPrefRowCount(14);
        VBox.setVgrow(codeArea, Priority.ALWAYS);

        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button rateBtn = new Button("⭐ Rate & Review");
        rateBtn.getStyleClass().add("button-secondary");
        TooltipHelper.install(rateBtn, "Submit a star rating and review for this snippet");
        rateBtn.setOnAction(e -> handleRateSnippet(snippet.id()));

        if (!snippet.isPurchasedByCurrentUser() && snippet.price().compareTo(BigDecimal.ZERO) > 0) {
            Button buyBtn = new Button("🛒 Buy Now for $" + snippet.price());
            buyBtn.getStyleClass().add("button");
            TooltipHelper.install(buyBtn, "Purchase this snippet to unlock and view the full source code");
            buyBtn.setOnAction(e -> {
                buyBtn.setDisable(true);
                marketApi.purchaseSnippet(snippet.id()).whenComplete((resp, err) -> {
                    Platform.runLater(() -> {
                        buyBtn.setDisable(false);
                        if (err != null) {
                            AlertHelper.showError("Purchase Failed", extractMessage(err));
                        } else {
                            AlertHelper.showSuccess("Success", resp.message());
                            codeArea.setText(resp.codeContent());
                            loadSnippets();
        loadProjects();
                        }
                    });
                });
            });
            actionRow.getChildren().addAll(rateBtn, buyBtn);
        } else {
            Label ownedLabel = new Label("Unlocked & Ready to Use");
            ownedLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            actionRow.getChildren().addAll(ownedLabel, rateBtn);
        }

        contentBox.getChildren().addAll(header, desc, codeArea, actionRow);
        pane.setContent(contentBox);
        dialog.showAndWait();
    }

    private void handleRateSnippet(Long snippetId) {
        Dialog<RatingCreateRequest> dialog = new Dialog<>();
        dialog.setTitle("Rate Code Snippet");
        dialog.setHeaderText("Share your review and rating");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
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

        ComboBox<Integer> starsCombo = new ComboBox<>();
        starsCombo.getItems().addAll(5, 4, 3, 2, 1);
        starsCombo.setValue(5);

        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Optional review comments...");
        reviewArea.setPrefRowCount(3);

        grid.add(new Label("Rating Stars:"), 0, 0);
        grid.add(starsCombo, 1, 0);
        grid.add(new Label("Review:"), 0, 1);
        grid.add(reviewArea, 1, 1);

        pane.setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new RatingCreateRequest(starsCombo.getValue(), reviewArea.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(req -> {
            marketApi.rateSnippet(snippetId, req).whenComplete((r, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Rating Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Thank You", "Your rating has been recorded!");
                        loadSnippets();
        loadProjects();
                    }
                });
            });
        });
    }

    @FXML
    private void handleSellSnippetDialog() {
        Dialog<SnippetCreateRequest> dialog = new Dialog<>();
        dialog.setTitle("List Code Snippet for Sale");
        dialog.setHeaderText("Share your code snippet or algorithm on the marketplace");

        DialogPane pane = dialog.getDialogPane();
        ButtonType publishBtn = new ButtonType("Publish", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(publishBtn, ButtonType.CANCEL);
        try {
            pane.getStylesheets().addAll(
                    getClass().getResource("/css/theme-dark.css").toExternalForm(),
                    getClass().getResource("/css/components.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));

        TextField titleField = new TextField();
        titleField.setPromptText("Snippet Title (e.g. Fast Fourier Transform in Java)");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Brief description of how to use this snippet...");
        descArea.setPrefRowCount(2);

        TextArea codeArea = new TextArea();
        codeArea.setPromptText("Paste your code here...");
        codeArea.setPrefRowCount(8);
        codeArea.getStyleClass().add("code-view");

        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("Java", "Python", "JavaScript", "TypeScript", "Rust", "Go", "C++", "SQL");
        langCombo.setValue("Java");

        TextField priceField = new TextField("0.00");
        priceField.setPromptText("Price in USD (0 for Free)");

        TextField tagsField = new TextField();
        tagsField.setPromptText("Comma-separated tags (e.g. math, algorithm, concurrency)");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Code Content:"), 0, 2);
        grid.add(codeArea, 1, 2);
        grid.add(new Label("Language:"), 0, 3);
        grid.add(langCombo, 1, 3);
        grid.add(new Label("Price ($):"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(new Label("Tags:"), 0, 5);
        grid.add(tagsField, 1, 5);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == publishBtn) {
                if (titleField.getText().isBlank() || codeArea.getText().isBlank()) {
                    return null;
                }
                BigDecimal price = BigDecimal.ZERO;
                try {
                    price = new BigDecimal(priceField.getText().trim());
                } catch (Exception ignored) {}

                List<String> tags = tagsField.getText().isBlank() ? List.of() :
                        Arrays.stream(tagsField.getText().split(",")).map(String::trim).toList();

                return new SnippetCreateRequest(
                        titleField.getText().trim(),
                        descArea.getText(),
                        codeArea.getText(),
                        langCombo.getValue().toLowerCase(),
                        price,
                        tags
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(req -> {
            marketApi.createSnippet(req).whenComplete((created, err) -> {
                Platform.runLater(() -> {
                    if (err != null) {
                        AlertHelper.showError("Publishing Failed", extractMessage(err));
                    } else {
                        AlertHelper.showSuccess("Published", "Your snippet '" + created.title() + "' is now listed on the marketplace!");
                        loadSnippets();
        loadProjects();
                    }
                });
            });
        });
    }

    private String extractMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : "Unknown error";
    }
}



