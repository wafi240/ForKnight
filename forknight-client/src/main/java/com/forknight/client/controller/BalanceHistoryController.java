package com.forknight.client.controller;

import com.forknight.client.api.MarketplaceApi;
import com.forknight.common.dto.market.TransactionDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class BalanceHistoryController {

    @FXML private Label currentBalanceLabel;
    @FXML private TableView<TransactionDTO> transactionTable;
    @FXML private TableColumn<TransactionDTO, String> colDate;
    @FXML private TableColumn<TransactionDTO, String> colType;
    @FXML private TableColumn<TransactionDTO, String> colAmount;
    @FXML private TableColumn<TransactionDTO, String> colItemType;
    @FXML private TableColumn<TransactionDTO, String> colCounterparty;

    private final MarketplaceApi marketplaceApi = new MarketplaceApi();
    private final ObservableList<TransactionDTO> transactionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (transactionTable != null) {
            colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().createdAt() != null ? data.getValue().createdAt().toString() : ""));
            colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().type()));
            colAmount.setCellValueFactory(data -> new SimpleStringProperty(
                    (data.getValue().type().equals("SPEND") ? "-" : "+") + "$" + data.getValue().amount().toString()
            ));
            colItemType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().itemType()));
            colCounterparty.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().counterparty()));

            transactionTable.setItems(transactionList);
        }
        loadData();
    }

    @FXML
    public void loadData() {
        marketplaceApi.getMyBalance().whenComplete((balance, error) -> {
            Platform.runLater(() -> {
                if (error == null && balance != null) {
                    currentBalanceLabel.setText("$" + balance.toString());
                }
            });
        });

        marketplaceApi.getTransactionHistory().whenComplete((transactions, error) -> {
            Platform.runLater(() -> {
                if (error == null && transactions != null) {
                    transactionList.setAll(transactions);
                }
            });
        });
    }
}
