package com.forknight.client.component;

import com.forknight.common.dto.ai.AiReviewDTO;
import com.forknight.common.dto.ai.AiReviewIssueDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import com.forknight.client.util.TooltipHelper;
import com.forknight.client.util.TooltipHelper;

public class AiReviewCardComponent extends VBox {

    public AiReviewCardComponent(AiReviewDTO review) {
        setSpacing(12);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        // Top Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label modelLabel = new Label("🤖 " + review.modelUsed());
        modelLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e2e8f0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Score Badge
        int score = review.score();
        Label scoreBadge = new Label("Score: " + score + "/10");
        String scoreColor = score >= 8 ? "#22c55e" : score >= 6 ? "#f59e0b" : "#ef4444";
        scoreBadge.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-size: 12px;", scoreColor));
        TooltipHelper.install(scoreBadge, "AI assessed code quality score (out of 10)");

        Label dateLabel = new Label(review.createdAt() != null ? review.createdAt().toString() : "");
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        header.getChildren().addAll(modelLabel, spacer, dateLabel, scoreBadge);

        // Summary Box
        Label summaryLabel = new Label(review.summary());
        summaryLabel.setWrapText(true);
        summaryLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px; -fx-padding: 8 12; -fx-background-color: #0d1117; -fx-background-radius: 6px;");

        getChildren().addAll(header, summaryLabel);

        // Issues List
        if (review.issues() != null && !review.issues().isEmpty()) {
            Label issuesHeader = new Label("Findings & Actionable Recommendations (" + review.issues().size() + "):");
            issuesHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            getChildren().add(issuesHeader);

            VBox issuesList = new VBox(8);
            for (AiReviewIssueDTO issue : review.issues()) {
                VBox issueCard = new VBox(6);
                issueCard.setPadding(new Insets(10));
                issueCard.setStyle("-fx-background-color: #0d1117; -fx-border-color: #30363d; -fx-border-radius: 6px; -fx-background-radius: 6px;");

                HBox issueHeader = new HBox(8);
                issueHeader.setAlignment(Pos.CENTER_LEFT);

                Label sevBadge = new Label(issue.severity());
                String sevColor = switch (issue.severity()) {
                    case "CRITICAL" -> "#ef4444";
                    case "HIGH" -> "#f97316";
                    case "MEDIUM" -> "#f59e0b";
                    default -> "#3b82f6";
                };
                sevBadge.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: #ffffff; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4px;", sevColor));
                TooltipHelper.install(sevBadge, "Issue severity: " + issue.severity());

                Label typeLabel = new Label("[" + issue.type() + "]");
                typeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");

                Label locLabel = new Label(issue.file() + (issue.line() != null ? ":" + issue.line() : ""));
                locLabel.setStyle("-fx-text-fill: #60a5fa; -fx-font-family: monospace; -fx-font-size: 11px;");

                issueHeader.getChildren().addAll(sevBadge, typeLabel, locLabel);

                Label msgLabel = new Label(issue.message());
                msgLabel.setWrapText(true);
                msgLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");

                HBox fixRow = new HBox(8);
                fixRow.setAlignment(Pos.CENTER_LEFT);
                Label fixLabel = new Label("💡 Fix: " + issue.suggestion());
                fixLabel.setWrapText(true);
                fixLabel.setStyle("-fx-text-fill: #86efac; -fx-font-size: 11px; -fx-font-family: monospace;");
                HBox.setHgrow(fixLabel, Priority.ALWAYS);

                Button copyBtn = new Button("Copy Fix");
                copyBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6; -fx-background-color: #21262d; -fx-text-fill: #e2e8f0; -fx-border-color: #30363d; -fx-border-radius: 4px;");
                TooltipHelper.install(copyBtn, "Copy this suggested code fix to your clipboard");
                copyBtn.setOnAction(e -> {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(issue.suggestion());
                    Clipboard.getSystemClipboard().setContent(content);
                    copyBtn.setText("Copied! ✓");
                });

                fixRow.getChildren().addAll(fixLabel, copyBtn);

                issueCard.getChildren().addAll(issueHeader, msgLabel, fixRow);
                issuesList.getChildren().add(issueCard);
            }
            getChildren().add(issuesList);
        }
    }
}
