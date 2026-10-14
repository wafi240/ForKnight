package com.forknight.client.component;

import com.forknight.common.dto.diff.DiffEntryDTO;
import com.forknight.common.dto.diff.DiffLineDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class DiffViewerComponent extends VBox {

    private final VBox filesContainer = new VBox(15);

    public DiffViewerComponent() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #0d1117;");

        ScrollPane scrollPane = new ScrollPane(filesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0d1117; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().add(scrollPane);
    }

    public void setDiffs(List<DiffEntryDTO> diffs) {
        filesContainer.getChildren().clear();

        if (diffs == null || diffs.isEmpty()) {
            Label empty = new Label("No file differences found.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-padding: 20px;");
            filesContainer.getChildren().add(empty);
            return;
        }

        for (DiffEntryDTO fileDiff : diffs) {
            VBox fileCard = new VBox();
            fileCard.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-radius: 6px; -fx-background-radius: 6px;");

            // Header Row
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(10, 15, 10, 15));
            header.setStyle("-fx-background-color: #21262d; -fx-background-radius: 6px 6px 0 0; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");

            String displayPath = fileDiff.newPath() != null && !"/dev/null".equals(fileDiff.newPath()) ?
                    fileDiff.newPath() : fileDiff.oldPath();

            Label pathLabel = new Label("📄 " + displayPath);
            pathLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0; -fx-font-size: 13px;");
            HBox.setHgrow(pathLabel, Priority.ALWAYS);

            Label badgeType = new Label(fileDiff.changeType());
            badgeType.setStyle("-fx-background-color: #334155; -fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4px;");

            Label additionsLabel = new Label("+" + fileDiff.additions());
            additionsLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-family: monospace;");

            Label deletionsLabel = new Label("-" + fileDiff.deletions());
            deletionsLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-family: monospace;");

            header.getChildren().addAll(pathLabel, badgeType, additionsLabel, deletionsLabel);
            fileCard.getChildren().add(header);

            // Diff Lines Table / Grid
            VBox linesBox = new VBox(0);
            linesBox.setPadding(new Insets(5, 0, 5, 0));

            for (DiffLineDTO line : fileDiff.lines()) {
                HBox lineRow = new HBox(8);
                lineRow.setAlignment(Pos.CENTER_LEFT);
                lineRow.setPadding(new Insets(1, 10, 1, 10));

                Label oldNum = new Label(line.oldLineNo() != null ? String.valueOf(line.oldLineNo()) : "");
                oldNum.setPrefWidth(35);
                oldNum.setAlignment(Pos.CENTER_RIGHT);
                oldNum.setStyle("-fx-text-fill: #475569; -fx-font-family: monospace; -fx-font-size: 11px;");

                Label newNum = new Label(line.newLineNo() != null ? String.valueOf(line.newLineNo()) : "");
                newNum.setPrefWidth(35);
                newNum.setAlignment(Pos.CENTER_RIGHT);
                newNum.setStyle("-fx-text-fill: #475569; -fx-font-family: monospace; -fx-font-size: 11px;");

                String prefix = switch (line.type()) {
                    case ADD -> "+ ";
                    case DELETE -> "- ";
                    case HEADER -> "@@ ";
                    default -> "  ";
                };

                Label contentLabel = new Label(prefix + line.content());
                contentLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
                HBox.setHgrow(contentLabel, Priority.ALWAYS);

                switch (line.type()) {
                    case ADD -> {
                        lineRow.setStyle("-fx-background-color: rgba(34, 197, 94, 0.15);");
                        contentLabel.setStyle("-fx-text-fill: #86efac; -fx-font-family: monospace; -fx-font-size: 12px;");
                    }
                    case DELETE -> {
                        lineRow.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15);");
                        contentLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-family: monospace; -fx-font-size: 12px;");
                    }
                    case HEADER -> {
                        lineRow.setStyle("-fx-background-color: rgba(59, 130, 246, 0.15);");
                        contentLabel.setStyle("-fx-text-fill: #93c5fd; -fx-font-family: monospace; -fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                    default -> {
                        contentLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-family: monospace; -fx-font-size: 12px;");
                    }
                }

                lineRow.getChildren().addAll(oldNum, newNum, contentLabel);
                linesBox.getChildren().add(lineRow);
            }

            fileCard.getChildren().add(linesBox);
            filesContainer.getChildren().add(fileCard);
        }
    }
}
