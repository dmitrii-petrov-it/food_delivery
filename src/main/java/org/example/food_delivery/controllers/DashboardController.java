package org.example.food_delivery.controllers;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.food_delivery.FoodDelivery;
import org.example.food_delivery.model.order.Order;
import org.example.food_delivery.model.order.OrderStatus;
import org.example.food_delivery.model.user.Client;
import org.example.food_delivery.model.user.Courier;
import org.example.food_delivery.model.user.CourierStatus;
import org.example.food_delivery.model.user.CourierVehicleType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DashboardController {
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM");

    private AppContext context;

    @FXML
    private Label dashboardTotalOrdersLabel;
    @FXML
    private Label dashboardDeliveredLabel;
    @FXML
    private Label dashboardActiveCouriersLabel;
    @FXML
    private Label dashboardTotalIncomeLabel;
    @FXML
    private Label dashboardOrdersTrendCaption;
    @FXML
    private Label dashboardSuccessRateCaption;
    @FXML
    private PieChart topRestaurantsChart;
    @FXML
    private PieChart courierVehicleChart;
    @FXML
    private LineChart<String, Number> ordersTrendChart;

    @FXML
    private void onExportReport() {
        if (context == null) {
            showError("Nothing to export yet.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export dashboard report");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        chooser.setInitialFileName("dashboard-report.csv");
        File file = chooser.showSaveDialog(null);
        if (file == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Metric,Value");
            writer.newLine();
            writer.write("Total Orders," + context.getOrders().size());
            writer.newLine();
            writer.write("Delivered Orders," + countDelivered());
            writer.newLine();
            writer.write("Active Couriers," + countActiveCouriers());
            writer.newLine();
            writer.write("Total Income," + computeTotalIncome().toPlainString());
            writer.newLine();
            writer.newLine();
            writer.write("Orders by Status");
            writer.newLine();
            for (OrderStatus status : OrderStatus.values()) {
                long count = context.getOrders().stream().filter(o -> o.getStatus() == status).count();
                writer.write(formatOrderStatus(status) + "," + count);
                writer.newLine();
            }
            writer.newLine();
            writer.write("Couriers by Vehicle");
            writer.newLine();
            for (CourierVehicleType type : CourierVehicleType.values()) {
                long count = context.getCouriers().stream().filter(c -> c.getVehicleType() == type).count();
                writer.write(type.name() + "," + count);
                writer.newLine();
            }
            showInfo("Report exported successfully.");
        } catch (IOException ex) {
            showError("Failed to export report: " + ex.getMessage());
        }
    }

    @FXML
    private void onBackToMainMenu(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FoodDelivery.openMainMenu(stage);
    }

    public void init(AppContext context) {
        this.context = context;
        context.getOrders().addListener((ListChangeListener<Order>) change -> refreshDashboard());
        context.getClients().addListener((ListChangeListener<Client>) change -> refreshDashboard());
        context.getCouriers().addListener((ListChangeListener<Courier>) change -> refreshDashboard());
        refreshDashboard();
    }

    private void refreshDashboard() {
        long totalOrders = context.getOrders().size();
        long deliveredOrders = countDelivered();
        long activeCouriers = countActiveCouriers();
        BigDecimal totalIncome = computeTotalIncome();

        if (dashboardTotalOrdersLabel != null) {
            dashboardTotalOrdersLabel.setText(String.valueOf(totalOrders));
        }
        if (dashboardDeliveredLabel != null) {
            dashboardDeliveredLabel.setText(String.valueOf(deliveredOrders));
        }
        if (dashboardActiveCouriersLabel != null) {
            dashboardActiveCouriersLabel.setText(String.valueOf(activeCouriers));
        }
        if (dashboardTotalIncomeLabel != null) {
            dashboardTotalIncomeLabel.setText(totalIncome.setScale(2, RoundingMode.HALF_UP).toPlainString() + " MDL");
        }
        if (dashboardOrdersTrendCaption != null) {
            long last7Days = ordersInLast7Days();
            dashboardOrdersTrendCaption.setText(last7Days + " orders in the last 7 days");
        }
        if (dashboardSuccessRateCaption != null) {
            double rate = totalOrders == 0 ? 0.0 : (deliveredOrders * 100.0) / totalOrders;
            dashboardSuccessRateCaption.setText(String.format("%.1f%% delivery success rate", rate));
        }

        updateTopRestaurantsChart();
        updateCourierVehicleChart();
        updateOrdersTrendChart();
    }

    private long countDelivered() {
        return context.getOrders().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .count();
    }

    private long countActiveCouriers() {
        return context.getCouriers().stream()
                .filter(courier -> courier.getStatus() == CourierStatus.AVAILABLE
                        || courier.getStatus() == CourierStatus.BUSY)
                .count();
    }

    private BigDecimal computeTotalIncome() {
        return context.getOrders().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(order -> {
                    BigDecimal price = order.getOrderPrice() == null ? BigDecimal.ZERO : order.getOrderPrice();
                    BigDecimal fee = order.getDeliveryFee() == null ? BigDecimal.ZERO : order.getDeliveryFee();
                    return price.add(fee);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long ordersInLast7Days() {
        LocalDate cutoff = LocalDate.now().minusDays(6);
        return context.getOrders().stream()
                .filter(order -> order.getCreatedAt() != null
                        && !order.getCreatedAt().toLocalDate().isBefore(cutoff))
                .count();
    }

    private void updateTopRestaurantsChart() {
        if (topRestaurantsChart == null) {
            return;
        }
        Map<String, Long> byRestaurant = context.getOrders().stream()
                .filter(order -> order.getRestaurantName() != null && !order.getRestaurantName().trim().isEmpty())
                .collect(Collectors.groupingBy(Order::getRestaurantName, Collectors.counting()));

        if (byRestaurant.isEmpty()) {
            // Sample placeholder slices so the chart never appears blank
            topRestaurantsChart.getData().setAll(
                    new PieChart.Data("Burger Place — sample (3)", 3),
                    new PieChart.Data("Pizza Corner — sample (2)", 2),
                    new PieChart.Data("Sushi Hub — sample (1)", 1)
            );
            return;
        }

        java.util.List<PieChart.Data> slices = byRestaurant.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()))
                .collect(Collectors.toList());
        topRestaurantsChart.getData().setAll(slices);
    }

    private void updateCourierVehicleChart() {
        if (courierVehicleChart == null) {
            return;
        }
        if (context.getCouriers().isEmpty()) {
            courierVehicleChart.getData().setAll(new PieChart.Data("No couriers yet", 1));
            return;
        }
        courierVehicleChart.getData().setAll(
                createVehicleSlice(CourierVehicleType.BIKE, "Bike"),
                createVehicleSlice(CourierVehicleType.SCOOTER, "Scooter"),
                createVehicleSlice(CourierVehicleType.CAR, "Car")
        );
    }

    private void updateOrdersTrendChart() {
        if (ordersTrendChart == null) {
            return;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<LocalDate, Long> countsByDay = context.getOrders().stream()
                .filter(order -> order.getCreatedAt() != null)
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate(), TreeMap::new, Collectors.counting()));

        if (countsByDay.isEmpty()) {
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate day = today.minusDays(i);
                series.getData().add(new XYChart.Data<>(day.format(DAY_FORMAT), 0));
            }
        } else {
            LocalDate end = countsByDay.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
            for (int i = 6; i >= 0; i--) {
                LocalDate day = end.minusDays(i);
                series.getData().add(new XYChart.Data<>(day.format(DAY_FORMAT), countsByDay.getOrDefault(day, 0L)));
            }
        }

        ordersTrendChart.getData().clear();
        ordersTrendChart.getData().add(series);
    }

    private PieChart.Data createVehicleSlice(CourierVehicleType type, String label) {
        long count = context.getCouriers().stream()
                .filter(courier -> courier.getVehicleType() == type)
                .count();
        return new PieChart.Data(label + " (" + count + ")", count);
    }

    private String formatOrderStatus(OrderStatus status) {
        String normalized = status.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Export");
        alert.setHeaderText("Report");
        styleAlert(alert);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Export");
        alert.setHeaderText("Report");
        styleAlert(alert);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.getStyleClass().add("app-dialog");
        ThemeManager.applyTo(pane);
    }
}
