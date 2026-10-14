package com.forknight.client.component;

import com.forknight.common.dto.repo.FileNodeDTO;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.List;
import java.util.function.Consumer;
import com.forknight.client.util.TooltipHelper;
import com.forknight.client.util.TooltipHelper;

public class FileTreeView extends TreeView<FileNodeDTO> {

    private Consumer<FileNodeDTO> onFileSelected;

    public FileTreeView() {
        setShowRoot(false);
        setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(FileNodeDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String icon = item.isDirectory() ? "📁 " : "📄 ";
                    setText(icon + item.name());
                    TooltipHelper.install(this, item.isDirectory() ? "Directory: " + item.path() : "File: " + item.path());
                    getStyleClass().add("file-tree-cell");
                }
            }
        });

        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null && !newVal.getValue().isDirectory()) {
                if (onFileSelected != null) {
                    onFileSelected.accept(newVal.getValue());
                }
            }
        });
    }

    public void setOnFileSelected(Consumer<FileNodeDTO> onFileSelected) {
        this.onFileSelected = onFileSelected;
    }

    public void populateTree(List<FileNodeDTO> nodes) {
        TreeItem<FileNodeDTO> rootItem = new TreeItem<>(FileNodeDTO.directory("root", "", List.of()));
        rootItem.setExpanded(true);

        for (FileNodeDTO node : nodes) {
            rootItem.getChildren().add(buildTreeItem(node));
        }

        setRoot(rootItem);
    }

    private TreeItem<FileNodeDTO> buildTreeItem(FileNodeDTO node) {
        TreeItem<TreeItem<FileNodeDTO>> unused;
        TreeItem<FileNodeDTO> item = new TreeItem<>(node);
        if (node.isDirectory() && node.children() != null) {
            item.setExpanded(true);
            for (FileNodeDTO child : node.children()) {
                item.getChildren().add(buildTreeItem(child));
            }
        }
        return item;
    }
}
