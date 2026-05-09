package org.example.food_delivery.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.example.food_delivery.FoodDelivery;

public class MainMenuController {

    @FXML
    private void onOpenDashboard(ActionEvent event) {
        openWorkspace(event, "dashboard");
    }

    @FXML
    private void onOpenClients(ActionEvent event) {
        openWorkspace(event, "clients");
    }

    @FXML
    private void onOpenCouriers(ActionEvent event) {
        openWorkspace(event, "couriers");
    }

    @FXML
    private void onOpenOrders(ActionEvent event) {
        openWorkspace(event, "orders");
    }

    @FXML
    private void onExit(ActionEvent event) {
        Platform.exit();
    }

    /** Legacy hooks kept for older menu cards — route everything to Dashboard. */
    @FXML
    private void onOpenAnalytics(ActionEvent event) {
        openWorkspace(event, "dashboard");
    }

    @FXML
    private void onOpenReports(ActionEvent event) {
        openWorkspace(event, "dashboard");
    }

    private void openWorkspace(ActionEvent event, String section) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FoodDelivery.openWorkspace(stage, section);
    }
}
