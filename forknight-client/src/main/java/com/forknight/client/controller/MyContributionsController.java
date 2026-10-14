package com.forknight.client.controller;

import com.forknight.client.api.ContribApi;
import com.forknight.client.util.FxmlNavigator;
import com.forknight.client.util.TooltipHelper;
import com.forknight.common.dto.contrib.ContribRequestDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MyContributionsController {

    @FXML private TableView<ContribRequestDTO> contribTable;
    @FXML private TableColumn<ContribRequestDTO, String> colRepo;
    @FXML private TableColumn<ContribRequestDTO, String> colOwner;
    @FXML private TableColumn<ContribRequestDTO, String> colStatus;
    @FXML private TableColumn<ContribRequestDTO, String> colAccess;
    @FXML private TableColumn<ContribRequestDTO, Void> colAction;

    private final ContribApi contribApi = new ContribApi();
    private final ObservableList<ContribRequestDTO> contribList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (contribTable != null) {
            colRepo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().repoName()));
            colOwner.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().ownerUsername()));
            colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status() != null ? data.getValue().status().name() : ""));
            colAccess.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().accessLevel() != null ? data.getValue().accessLevel().name() : ""));

            colAction.setCellFactory(param -> new TableCell<>() {
                private final Button btn = new Button("Open Repo");
                {
                    btn.getStyleClass().add("button-secondary");
                    btn.setOnAction(event -> {
                        ContribRequestDTO req = getTableView().getItems().get(getIndex());
                        if (req.status() != null && req.status().name().equals("APPROVED")) {
                            RepoDetailController controller = FxmlNavigator.navigateToWithController("repo_detail.fxml");
                            if (controller != null) {
                                controller.initRepo(req.repoId());
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Not Approved");
                            alert.setHeaderText(null);
                            alert.setContentText("Your contribution request has not been approved yet.");
                            alert.showAndWait();
                        }
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        ContribRequestDTO req = getTableView().getItems().get(getIndex());
                        btn.setDisable(req.status() == null || !req.status().name().equals("APPROVED"));
                        setGraphic(btn);
                    }
                }
            });

            contribTable.setItems(contribList);
        }
        loadMyContributions();
    }

    @FXML
    private void loadMyContributions() {
        contribApi.getMyRequests().whenComplete((requests, error) -> {
            Platform.runLater(() -> {
                if (error != null) {
                    System.err.println("Could not load contributions: " + error.getMessage());
                } else {
                    contribList.setAll(requests);
                }
            });
        });
    }
}
