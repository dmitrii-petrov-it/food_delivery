package org.example.food_delivery.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.example.food_delivery.FoodDelivery;
import org.example.food_delivery.model.user.Courier;
import org.example.food_delivery.model.user.CourierStatus;
import org.example.food_delivery.model.user.CourierVehicleType;

import java.util.Optional;
import java.util.function.UnaryOperator;

public class CouriersController {
    private AppContext context;
    private FilteredList<Courier> filteredCouriers;

    @FXML
    private TableView<Courier> courierTable;
    @FXML
    private TableColumn<Courier, Integer> courierIdColumn;
    @FXML
    private TableColumn<Courier, String> courierNameColumn;
    @FXML
    private TableColumn<Courier, String> courierPhoneColumn;
    @FXML
    private TableColumn<Courier, String> courierVehicleColumn;
    @FXML
    private TableColumn<Courier, CourierStatus> courierStatusColumn;
    @FXML
    private TableColumn<Courier, Void> courierActionsColumn;
    @FXML
    private Label totalCouriersLabel;
    @FXML
    private Label availableCouriersLabel;
    @FXML
    private Label busyInactiveCouriersLabel;
    @FXML
    private TextField courierSearchField;
    @FXML
    private ComboBox<String> courierStatusFilterBox;
    @FXML
    private ComboBox<String> courierVehicleFilterBox;
    @FXML
    private TextField courierExtraFilterField;

    public void init(AppContext context) {
        this.context = context;
        setupCourierTable();
        setupFilters();
        try {
            refreshCouriers();
        } catch (Exception ex) {
            System.err.println("CouriersController init: failed to load couriers — " + ex.getMessage());
        }
    }

    private void setupCourierTable() {
        // show sequential index instead of DB id
        courierIdColumn.setCellFactory(col -> new TableCell<Courier, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        courierNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        courierPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        courierVehicleColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatVehicle(cell.getValue().getVehicleType())));
        courierStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        courierStatusColumn.setCellFactory(column -> new TableCell<Courier, CourierStatus>() {
            private final Label badge = new Label();

            {
                badge.getStyleClass().add("status-badge");
            }

            @Override
            protected void updateItem(CourierStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                badge.setText(formatStatus(status));
                badge.getStyleClass().removeIf(style -> style.startsWith("courier-status-"));
                badge.getStyleClass().add(statusClass(status));
                setGraphic(badge);
                setText(null);
            }
        });
        setupActionsColumn();
        filteredCouriers = new FilteredList<>(context.getCouriers(), courier -> true);
        courierTable.setItems(filteredCouriers);
    }

    private void setupFilters() {
        courierStatusFilterBox.getItems().setAll("All Statuses", "Available", "Busy", "Inactive");
        courierVehicleFilterBox.getItems().setAll("All Vehicles", "Bike", "Scooter", "Car");
        courierStatusFilterBox.getSelectionModel().selectFirst();
        courierVehicleFilterBox.getSelectionModel().selectFirst();
        courierSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        courierStatusFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        courierVehicleFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        courierExtraFilterField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        if (filteredCouriers == null) {
            return;
        }
        filteredCouriers.setPredicate(courier ->
                matchesSearch(courier) &&
                matchesStatusFilter(courier) &&
                matchesVehicleFilter(courier) &&
                matchesExtraFilter(courier));
    }

    private boolean matchesSearch(Courier courier) {
        String query = normalize(courierSearchField.getText());
        if (query.isEmpty()) {
            return true;
        }
        return contains(courier.getFullName(), query)
                || contains(courier.getPhone(), query)
                || contains(formatVehicle(courier.getVehicleType()), query)
                || contains(formatStatus(courier.getStatus()), query);
    }

    private boolean matchesStatusFilter(Courier courier) {
        String filter = courierStatusFilterBox.getValue();
        return filter == null || "All Statuses".equals(filter) || formatStatus(courier.getStatus()).equalsIgnoreCase(filter);
    }

    private boolean matchesVehicleFilter(Courier courier) {
        String filter = courierVehicleFilterBox.getValue();
        return filter == null || "All Vehicles".equals(filter) || formatVehicle(courier.getVehicleType()).equalsIgnoreCase(filter);
    }

    private boolean matchesExtraFilter(Courier courier) {
        String query = normalize(courierExtraFilterField.getText());
        return query.isEmpty()
                || contains(formatVehicle(courier.getVehicleType()), query)
                || contains(formatStatus(courier.getStatus()), query)
                || contains(courier.getPhone(), query);
    }

    private void setupActionsColumn() {
        courierActionsColumn.setCellFactory(column -> new TableCell<Courier, Void>() {
            private final Button editButton = createIconButton("\u270E", "table-edit-icon-button");
            private final Button deleteButton = createIconButton("\uD83D\uDDD1", "table-delete-icon-button");
            private final HBox box = new HBox(8.0, editButton, deleteButton);

            {
                box.setAlignment(Pos.CENTER);
                editButton.setOnAction(event -> {
                    Courier courier = getTableView().getItems().get(getIndex());
                    showCourierDialog("Edit Courier", "Update courier profile", courier)
                            .ifPresent(updated -> updateCourier(courier.getId(), updated));
                });
                deleteButton.setOnAction(event -> {
                    Courier courier = getTableView().getItems().get(getIndex());
                    try {
                        deleteCourier(courier);
                        refreshCouriers();
                    } catch (Exception ex) {
                        showError(ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });
    }

    @FXML
    private void onCourierAdd() {
        showCourierDialog("Add Courier", "Create a new courier", null).ifPresent(courier -> {
            try {
                context.getCourierService().create(courier);
                refreshCouriers();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onCourierRefresh() {
        courierSearchField.clear();
        courierExtraFilterField.clear();
        courierStatusFilterBox.getSelectionModel().selectFirst();
        courierVehicleFilterBox.getSelectionModel().selectFirst();
        refreshCouriers();
    }

    @FXML
    private void onBackToMainMenu(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FoodDelivery.openMainMenu(stage);
    }

    private Optional<Courier> showCourierDialog(String title, String header, Courier existingCourier) {
        Dialog<Courier> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        styleDialog(dialog.getDialogPane());

        boolean editing = existingCourier != null;
        ButtonType submitButtonType = new ButtonType(editing ? "Save Changes" : "Add Courier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        phoneField.setTextFormatter(createPhoneFormatter());
                        Label nameError = createErrorLabel();
                        Label phoneError = createErrorLabel();
                        Label vehicleError = createErrorLabel();
                        Label statusError = createErrorLabel();
        ComboBox<CourierVehicleType> vehicleBox = new ComboBox<>();
        ComboBox<CourierStatus> statusBox = new ComboBox<>();
        vehicleBox.getItems().setAll(CourierVehicleType.values());
        statusBox.getItems().setAll(CourierStatus.values());
        vehicleBox.getSelectionModel().select(CourierVehicleType.BIKE);
        statusBox.getSelectionModel().select(CourierStatus.AVAILABLE);

        if (editing) {
            nameField.setText(existingCourier.getFullName());
            phoneField.setText(existingCourier.getPhone());
            vehicleBox.getSelectionModel().select(existingCourier.getVehicleType());
            statusBox.getSelectionModel().select(existingCourier.getStatus());
        }

        GridPane form = new GridPane();
        form.getStyleClass().add("modal-form");
        form.setHgap(12.0);
        form.setVgap(10.0);
        int row = 0;
        form.add(new Label("Full Name"), 0, row);
        form.add(nameField, 1, row++);
        form.add(nameError, 1, row++);
        form.add(new Label("Phone"), 0, row);
        form.add(phoneField, 1, row++);
        form.add(phoneError, 1, row++);
        form.add(new Label("Vehicle"), 0, row);
        form.add(vehicleBox, 1, row++);
        form.add(vehicleError, 1, row++);
        form.add(new Label("Status"), 0, row);
        form.add(statusBox, 1, row++);
        form.add(statusError, 1, row++);
        dialog.getDialogPane().setContent(form);

        // inline validation
        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        Runnable validate = () -> {
            boolean valid = true;
            valid &= applyFieldError(nameField, nameError, isNonEmpty(nameField.getText()), "Full name is required.");
            valid &= applyFieldError(phoneField, phoneError, isValidPhone(phoneField.getText()), "Phone must be 12 characters total: optional '+' at start and 11 digits.");
            valid &= applyFieldError(vehicleBox, vehicleError, vehicleBox.getValue() != null, "Vehicle is required.");
            valid &= applyFieldError(statusBox, statusError, statusBox.getValue() != null, "Status is required.");
            submitButton.setDisable(!valid);
        };
        nameField.textProperty().addListener((obs, o, n) -> validate.run());
        phoneField.textProperty().addListener((obs, o, n) -> validate.run());
        vehicleBox.valueProperty().addListener((obs, o, n) -> validate.run());
        statusBox.valueProperty().addListener((obs, o, n) -> validate.run());
        validate.run();

        submitButton.addEventFilter(ActionEvent.ACTION, event -> {
            validate.run();
            if (submitButton.isDisabled()) {
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType != submitButtonType) {
                return null;
            }
            return new Courier(editing ? existingCourier.getId() : null,
                    nameField.getText(),
                    phoneField.getText(),
                    vehicleBox.getValue(),
                    statusBox.getValue());
        });
        return dialog.showAndWait();
    }

    private boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\+?\\d{11}");
    }

    private boolean applyFieldError(Control field, Label errorLabel, boolean valid, String message) {
        if (valid) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            field.getStyleClass().remove("input-error");
            return true;
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (!field.getStyleClass().contains("input-error")) {
            field.getStyleClass().add("input-error");
        }
        return false;
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.getStyleClass().add("field-error");
        label.setManaged(false);
        label.setVisible(false);
        return label;
    }

    private TextFormatter<String> createPhoneFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String next = change.getControlNewText();
            if (!next.matches("\\+?\\d*")) {
                return null;
            }
            if (next.indexOf('+') > 0) {
                return null;
            }
            String digits = next.replace("+", "");
            // exactly 11 digits allowed, optionally prefixed with '+'
            return digits.length() <= 11 ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    private void updateCourier(Integer courierId, Courier updatedCourier) {
        try {
            updatedCourier.setId(courierId);
            context.getCourierService().update(updatedCourier);
            refreshCouriers();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshCouriers() {
        context.getCouriers().setAll(context.getCourierService().findAll());
        applyFilters();
        updateStats();
    }

    private void updateStats() {
        int total = context.getCouriers().size();
        long available = context.getCouriers().stream().filter(courier -> courier.getStatus() == CourierStatus.AVAILABLE).count();
        long busyInactive = context.getCouriers().stream().filter(courier -> courier.getStatus() == CourierStatus.BUSY || courier.getStatus() == CourierStatus.INACTIVE).count();
        totalCouriersLabel.setText(String.valueOf(total));
        availableCouriersLabel.setText(String.valueOf(available));
        busyInactiveCouriersLabel.setText(String.valueOf(busyInactive));
    }

    private void deleteCourier(Courier courier) {
        if (courier == null) {
            throw new IllegalArgumentException("Courier is not selected");
        }
        if (!confirmDelete("courier", courier.getId())) {
            return;
        }
        context.getCourierService().deleteById(courier.getId());
    }

    private String formatVehicle(CourierVehicleType vehicleType) {
        if (vehicleType == null) {
            return "";
        }
        String value = vehicleType.toString();
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String formatStatus(CourierStatus status) {
        if (status == null) {
            return "";
        }
        String value = status.name().toLowerCase();
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String statusClass(CourierStatus status) {
        switch (status) {
            case BUSY:
                return "courier-status-busy";
            case INACTIVE:
                return "courier-status-inactive";
            default:
                return "courier-status-available";
        }
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private Button createIconButton(String icon, String styleClass) {
        Button button = new Button(icon);
        button.getStyleClass().add(styleClass);
        return button;
    }

    private void styleDialog(DialogPane dialogPane) {
        dialogPane.getStyleClass().add("app-dialog");
        ThemeManager.applyTo(dialogPane);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation failed");
        alert.setContentText(message == null ? "Unexpected error" : message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private boolean confirmDelete(String entityName, Integer id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm delete");
        alert.setHeaderText("Delete " + entityName + "?");
        alert.setContentText("This action cannot be undone.");
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void styleAlert(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.getStyleClass().add("app-dialog");
        ThemeManager.applyTo(pane);
    }

    // duplicate alert helpers removed
}
