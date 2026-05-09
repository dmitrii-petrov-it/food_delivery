package org.example.food_delivery.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.example.food_delivery.FoodDelivery;
import org.example.food_delivery.model.order.Order;
import org.example.food_delivery.model.order.OrderStatus;
import org.example.food_delivery.model.user.Client;
import org.example.food_delivery.model.user.Courier;
import org.example.food_delivery.model.user.CourierStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrdersController {
    private static final DateTimeFormatter DELIVERED_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private AppContext context;
    private final ObservableList<Courier> availableCouriers = FXCollections.observableArrayList();
    private FilteredList<Order> filteredOrders;
    private boolean dateRangeWarned = false;

    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, String> orderClientIdColumn;
    @FXML
    private TableColumn<Order, String> orderCourierIdColumn;
    @FXML
    private TableColumn<Order, String> orderNumberColumn;
    @FXML
    private TableColumn<Order, String> orderRestaurantColumn;
    @FXML
    private TableColumn<Order, BigDecimal> orderPriceColumn;
    @FXML
    private TableColumn<Order, OrderStatus> orderStatusColumn;
    @FXML
    private TableColumn<Order, LocalDateTime> orderCreatedColumn;
    @FXML
    private TableColumn<Order, LocalDateTime> orderDeliveredColumn;
    @FXML
    private TableColumn<Order, Void> actionsColumn;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label inDeliveryLabel;
    @FXML
    private Label deliveredLabel;
    @FXML
    private Label cancelledLabel;
    @FXML
    private TextField headerSearchField;
    @FXML
    private TextField tableSearchField;
    @FXML
    private ComboBox<String> statusFilterBox;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;

    public void init(AppContext context) {
        this.context = context;
        try {
            refreshAvailableCouriers();
        } catch (Exception ex) {
            System.err.println("OrdersController init: failed to load couriers — " + ex.getMessage());
        }
        context.getCouriers().addListener((ListChangeListener<Courier>) change -> refreshAvailableCouriers());
        setupOrderTable();
        setupFilters();
        try {
            refreshOrders();
        } catch (Exception ex) {
            System.err.println("OrdersController init: failed to load orders — " + ex.getMessage());
        }
    }

    private void setupOrderTable() {
        // show sequential row number instead of DB id
        orderIdColumn.setCellFactory(col -> new TableCell<Order, Integer>() {
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
        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        orderClientIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(resolveClientName(cell.getValue().getClientId())));
        orderRestaurantColumn.setCellValueFactory(new PropertyValueFactory<>("restaurantName"));
        orderCourierIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(resolveCourierName(cell.getValue().getCourierId())));
        orderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("orderPrice"));
        orderPriceColumn.setCellFactory(column -> new TableCell<Order, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.toPlainString() + " MDL");
            }
        });
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderStatusColumn.setCellFactory(column -> new TableCell<Order, OrderStatus>() {
            private final Label badge = new Label();

            {
                badge.getStyleClass().add("status-badge");
            }

            @Override
            protected void updateItem(OrderStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                badge.setText(formatStatus(status));
                badge.getStyleClass().removeIf(style -> style.startsWith("status-") && !style.equals("status-badge"));
                badge.getStyleClass().add(statusClass(status));
                setGraphic(badge);
                setText(null);
            }
        });
        orderCreatedColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getCreatedAt()));
        orderCreatedColumn.setCellFactory(column -> new TableCell<Order, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.format(TABLE_DATE_FORMAT));
            }
        });
        orderDeliveredColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDeliveredAt()));
        orderDeliveredColumn.setCellFactory(column -> new TableCell<Order, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setText(null);
                    return;
                }
                Order row = getTableRow() == null ? null : getTableRow().getItem();
                if (row != null && row.getStatus() == OrderStatus.DELIVERED && value != null) {
                    setText(value.toLocalTime().format(DELIVERED_TIME_FORMAT));
                } else {
                    setText(null);
                }
            }
        });
        setupActionsColumn();

        filteredOrders = new FilteredList<>(context.getOrders(), order -> true);
        orderTable.setItems(filteredOrders);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<Order, Void>() {
            private final Button editButton = createIconButton("\u270E", "table-edit-icon-button");
            private final Button deleteButton = createIconButton("\uD83D\uDDD1", "table-delete-icon-button");
            private final HBox box = new HBox(8.0, editButton, deleteButton);

            {
                box.setAlignment(Pos.CENTER);
                editButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDialog("Edit Order", "Update delivery order", order)
                            .ifPresent(updated -> updateOrder(order.getId(), updated));
                });
                deleteButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    try {
                        deleteOrder(order);
                        refreshOrders();
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

    private void setupFilters() {
        statusFilterBox.getItems().setAll("All Statuses", "Created", "Accepted", "Preparing", "In Delivery", "Delivered", "Cancelled");
        statusFilterBox.getSelectionModel().selectFirst();
        headerSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        tableSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        fromDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    @FXML
    private void onOrderAdd() {
        showOrderDialog("Add Order", "Create a new delivery order", null).ifPresent(order -> {
            try {
                Order saved = context.getOrderService().create(order);
                syncCourierStatusFor(saved, null);
                refreshOrders();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onOrderRefresh() {
        headerSearchField.clear();
        tableSearchField.clear();
        statusFilterBox.getSelectionModel().selectFirst();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        refreshOrders();
    }

    @FXML
    private void onBackToMainMenu(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FoodDelivery.openMainMenu(stage);
    }

    private Optional<Order> showOrderDialog(String title, String header, Order existingOrder) {
        Dialog<Order> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        styleDialog(dialog.getDialogPane());

        boolean editing = existingOrder != null;
        ButtonType submitButtonType = new ButtonType(editing ? "Save Changes" : "Add Order", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        ComboBox<Client> clientBox = createClientComboBox();
        ComboBox<Courier> courierBox = createCourierComboBox(existingOrder == null ? null : existingOrder.getCourierId());
        TextField numberField = new TextField();
        TextField restaurantField = new TextField();
        TextField foodField = new TextField();
        TextField addressField = new TextField();
        TextField priceField = new TextField();
        TextField feeField = new TextField();
        ComboBox<OrderStatus> statusBox = new ComboBox<>();
        TextField deliveredField = new TextField();
                        Label clientError = createErrorLabel();
                        Label restaurantError = createErrorLabel();
                        Label foodError = createErrorLabel();
                        Label addressError = createErrorLabel();
                        Label priceError = createErrorLabel();
                        Label feeError = createErrorLabel();
                        Label statusError = createErrorLabel();
                        Label deliveredError = createErrorLabel();
        statusBox.getItems().setAll(OrderStatus.values());
        statusBox.getSelectionModel().select(OrderStatus.CREATED);

        if (editing) {
            clientBox.getSelectionModel().select(findClientById(existingOrder.getClientId()));
            courierBox.getSelectionModel().select(findCourierById(existingOrder.getCourierId()));
            numberField.setText(existingOrder.getOrderNumber());
            restaurantField.setText(existingOrder.getRestaurantName());
            foodField.setText(existingOrder.getFoodDescription());
            addressField.setText(existingOrder.getDeliveryAddress());
            priceField.setText(existingOrder.getOrderPrice() == null ? "" : existingOrder.getOrderPrice().toPlainString());
            feeField.setText(existingOrder.getDeliveryFee() == null ? "" : existingOrder.getDeliveryFee().toPlainString());
            statusBox.getSelectionModel().select(existingOrder.getStatus());
            deliveredField.setText(existingOrder.getDeliveredAt() == null ? "" : existingOrder.getDeliveredAt().toLocalTime().format(DELIVERED_TIME_FORMAT));
        }

        GridPane form = new GridPane();
        form.getStyleClass().add("modal-form");
        form.setHgap(12.0);
        form.setVgap(10.0);
        int row = 0;
        form.add(new Label("Client"), 0, row);
        form.add(clientBox, 1, row++);
        form.add(clientError, 1, row++);
        form.add(new Label("Courier"), 0, row);
        form.add(courierBox, 1, row++);
        form.add(new Label("Order Number"), 0, row);
        form.add(numberField, 1, row++);
        form.add(new Label("Restaurant"), 0, row);
        form.add(restaurantField, 1, row++);
        form.add(restaurantError, 1, row++);
        form.add(new Label("Food"), 0, row);
        form.add(foodField, 1, row++);
        form.add(foodError, 1, row++);
        form.add(new Label("Address"), 0, row);
        form.add(addressField, 1, row++);
        form.add(addressError, 1, row++);
        form.add(new Label("Price"), 0, row);
        form.add(priceField, 1, row++);
        form.add(priceError, 1, row++);
        form.add(new Label("Fee"), 0, row);
        form.add(feeField, 1, row++);
        form.add(feeError, 1, row++);
        form.add(new Label("Status"), 0, row);
        form.add(statusBox, 1, row++);
        form.add(statusError, 1, row++);
        form.add(new Label("Delivered Time"), 0, row);
        form.add(deliveredField, 1, row++);
        form.add(deliveredError, 1, row++);
        dialog.getDialogPane().setContent(form);

        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        Runnable validate = () -> {
            boolean valid = true;
            valid &= applyFieldError(clientBox, clientError, clientBox.getValue() != null, "Client is required.");
            valid &= applyFieldError(restaurantField, restaurantError, hasText(restaurantField.getText()), "Restaurant is required.");
            valid &= applyFieldError(foodField, foodError, hasText(foodField.getText()), "Food description is required.");
            valid &= applyFieldError(addressField, addressError, hasText(addressField.getText()), "Address is required.");
            valid &= applyFieldError(priceField, priceError, isValidMoney(priceField.getText()), "Price must be a valid non-negative number.");
            valid &= applyFieldError(feeField, feeError, isValidMoney(feeField.getText()), "Fee must be a valid non-negative number.");
            valid &= applyFieldError(statusBox, statusError, statusBox.getValue() != null, "Status is required.");
            boolean requiresDelivered = statusBox.getValue() == OrderStatus.DELIVERED;
            boolean deliveredOk = !requiresDelivered || isValidDeliveredTime(deliveredField.getText());
            valid &= applyFieldError(deliveredField, deliveredError, deliveredOk, "Delivered time must be in HH:mm format.");
            submitButton.setDisable(!valid);
        };

        clientBox.valueProperty().addListener((obs, o, n) -> validate.run());
        restaurantField.textProperty().addListener((obs, o, n) -> validate.run());
        foodField.textProperty().addListener((obs, o, n) -> validate.run());
        addressField.textProperty().addListener((obs, o, n) -> validate.run());
        priceField.textProperty().addListener((obs, o, n) -> validate.run());
        feeField.textProperty().addListener((obs, o, n) -> validate.run());
        statusBox.valueProperty().addListener((obs, o, n) -> validate.run());
        deliveredField.textProperty().addListener((obs, o, n) -> validate.run());
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
            Order built = buildOrder(
                    editing ? existingOrder.getId() : null,
                    clientBox.getValue(),
                    courierBox.getValue(),
                    numberField.getText(),
                    restaurantField.getText(),
                    foodField.getText(),
                    addressField.getText(),
                    priceField.getText(),
                    feeField.getText(),
                    statusBox.getValue(),
                    deliveredField.getText(),
                    editing ? existingOrder.getCreatedAt() : null
            );
            if (editing) {
                built.setCreatedAt(existingOrder.getCreatedAt());
            }
            return built;
        });
        return dialog.showAndWait();
    }

    private boolean isValidMoney(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            return new BigDecimal(value.trim()).signum() >= 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isValidDeliveredTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            LocalTime.parse(value.trim(), DELIVERED_TIME_FORMAT);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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

    private Order buildOrder(Integer id,
                             Client client,
                             Courier courier,
                             String orderNumber,
                             String restaurantName,
                             String foodDescription,
                             String deliveryAddress,
                             String priceValue,
                             String feeValue,
                             OrderStatus status,
                             String deliveredTime,
                             LocalDateTime createdAtForEdit) {
        Order order = new Order();
        order.setId(id);
        if (client == null) {
            throw new IllegalArgumentException("Client is required");
        }
        order.setClientId(client.getId());
        order.setCourierId(courier == null ? null : courier.getId());
        order.setOrderNumber(orderNumber);
        order.setRestaurantName(restaurantName);
        order.setFoodDescription(foodDescription);
        order.setDeliveryAddress(deliveryAddress);
        order.setOrderPrice(parseBigDecimal(priceValue));
        order.setDeliveryFee(parseBigDecimal(feeValue));
        order.setStatus(status);
        order.setDeliveredAt(status == OrderStatus.DELIVERED
                ? parseDeliveredAt(deliveredTime, createdAtForEdit)
                : null);
        return order;
    }

    private void updateOrder(Integer orderId, Order updatedOrder) {
        try {
            updatedOrder.setId(orderId);
            Order previous = context.getOrders().stream()
                    .filter(o -> orderId.equals(o.getId()))
                    .findFirst().orElse(null);
            context.getOrderService().update(updatedOrder);
            syncCourierStatusFor(updatedOrder, previous);
            refreshOrders();
            if (context.getCouriers() != null) {
                context.getCouriers().setAll(context.getCourierService().findAll());
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    /**
     * Keeps couriers in step with the order they're assigned to:
     *   ACCEPTED / PREPARING / IN_DELIVERY → courier becomes BUSY
     *   DELIVERED / CANCELLED              → courier becomes AVAILABLE
     * If the courier on a previous version of the order was different and is now
     * unassigned, that ex-courier is freed back to AVAILABLE.
     */
    private void syncCourierStatusFor(Order order, Order previous) {
        if (order == null || context == null || context.getCourierService() == null) {
            return;
        }
        // Free up the previous courier if they were swapped out
        if (previous != null && previous.getCourierId() != null
                && !java.util.Objects.equals(previous.getCourierId(), order.getCourierId())) {
            tryUpdateCourierStatus(previous.getCourierId(), CourierStatus.AVAILABLE);
        }
        if (order.getCourierId() == null || order.getStatus() == null) {
            return;
        }
        switch (order.getStatus()) {
            case ACCEPTED:
            case PREPARING:
            case IN_DELIVERY:
                tryUpdateCourierStatus(order.getCourierId(), CourierStatus.BUSY);
                break;
            case DELIVERED:
            case CANCELLED:
                tryUpdateCourierStatus(order.getCourierId(), CourierStatus.AVAILABLE);
                break;
            default:
                // CREATED — leave courier status alone
        }
    }

    private void tryUpdateCourierStatus(Integer courierId, CourierStatus status) {
        try {
            context.getCourierService().updateStatus(courierId, status);
        } catch (Exception ex) {
            System.err.println("syncCourierStatusFor: " + ex.getMessage());
        }
    }

    private void refreshOrders() {
        context.getOrders().setAll(context.getOrderService().findAll());
        applyFilters();
        updateStats();
    }

    private void applyFilters() {
        if (filteredOrders == null) {
            return;
        }
        // validate date range — warn at most once until the user fixes it
        if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null
                && fromDatePicker.getValue().isAfter(toDatePicker.getValue())) {
            if (!dateRangeWarned) {
                dateRangeWarned = true;
                showError("Invalid date range: 'From' date must be earlier than or equal to 'To' date.");
            }
            // hide all rows while the range is invalid
            filteredOrders.setPredicate(order -> false);
            updateStats();
            return;
        }
        dateRangeWarned = false;
        filteredOrders.setPredicate(order -> matchesSearch(order) && matchesStatus(order) && matchesDateRange(order));
        updateStats();
    }

    private boolean matchesSearch(Order order) {
        String query = ((headerSearchField.getText() == null ? "" : headerSearchField.getText()) + " "
                + (tableSearchField.getText() == null ? "" : tableSearchField.getText())).trim().toLowerCase();
        if (query.isEmpty()) {
            return true;
        }
        return contains(order.getOrderNumber(), query)
                || contains(resolveClientName(order.getClientId()), query)
                || contains(resolveCourierName(order.getCourierId()), query)
                || contains(order.getRestaurantName(), query)
                || contains(order.getFoodDescription(), query)
                || contains(order.getDeliveryAddress(), query);
    }

    private boolean matchesStatus(Order order) {
        String value = statusFilterBox.getValue();
        if (value == null || "All Statuses".equals(value)) {
            return true;
        }
        return formatStatus(order.getStatus()).equalsIgnoreCase(value);
    }

    private boolean matchesDateRange(Order order) {
        if (order.getCreatedAt() == null) {
            return fromDatePicker.getValue() == null && toDatePicker.getValue() == null;
        }
        LocalDate createdDate = order.getCreatedAt().toLocalDate();
        if (fromDatePicker.getValue() != null && createdDate.isBefore(fromDatePicker.getValue())) {
            return false;
        }
        return toDatePicker.getValue() == null || !createdDate.isAfter(toDatePicker.getValue());
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private void updateStats() {
        totalOrdersLabel.setText(String.valueOf(context.getOrders().size()));
        inDeliveryLabel.setText(String.valueOf(context.getOrders().stream().filter(order -> order.getStatus() == OrderStatus.IN_DELIVERY).count()));
        deliveredLabel.setText(String.valueOf(context.getOrders().stream().filter(order -> order.getStatus() == OrderStatus.DELIVERED).count()));
        cancelledLabel.setText(String.valueOf(context.getOrders().stream().filter(order -> order.getStatus() == OrderStatus.CANCELLED).count()));
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount is required");
        }
        return new BigDecimal(value.trim());
    }

    private LocalDateTime parseDeliveredAt(String value, LocalDateTime createdAt) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivered time is required (HH:mm)");
        }
        try {
            LocalTime time = LocalTime.parse(value.trim(), DELIVERED_TIME_FORMAT);
            LocalDate date = createdAt == null ? LocalDate.now() : createdAt.toLocalDate();
            return date.atTime(time);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Delivered time must be in HH:mm format");
        }
    }

    private ComboBox<Client> createClientComboBox() {
        ComboBox<Client> comboBox = new ComboBox<>();
        comboBox.setItems(context.getClients());
        comboBox.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client client) {
                return client == null ? "" : client.getFullName();
            }

            @Override
            public Client fromString(String value) {
                return null;
            }
        });
        comboBox.setCellFactory(listView -> new ListCell<Client>() {
            @Override
            protected void updateItem(Client item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });
        return comboBox;
    }

    private ComboBox<Courier> createCourierComboBox(Integer includeCourierId) {
        ComboBox<Courier> comboBox = new ComboBox<>();
        ObservableList<Courier> items = FXCollections.observableArrayList(availableCouriers);
        // make sure the order's currently-assigned courier is selectable even if BUSY/INACTIVE
        if (includeCourierId != null) {
            Courier assigned = findCourierById(includeCourierId);
            if (assigned != null && items.stream().noneMatch(c -> includeCourierId.equals(c.getId()))) {
                items.add(0, assigned);
            }
        }
        comboBox.setItems(items);
        comboBox.setConverter(new StringConverter<Courier>() {
            @Override
            public String toString(Courier courier) {
                if (courier == null) return "";
                String suffix = (courier.getStatus() != null && courier.getStatus() != CourierStatus.AVAILABLE)
                        ? "  (" + courier.getStatus().name().toLowerCase() + ")" : "";
                return courier.getFullName() + suffix;
            }

            @Override
            public Courier fromString(String value) {
                return null;
            }
        });
        comboBox.setCellFactory(listView -> new ListCell<Courier>() {
            @Override
            protected void updateItem(Courier item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String suffix = (item.getStatus() != null && item.getStatus() != CourierStatus.AVAILABLE)
                        ? "  (" + item.getStatus().name().toLowerCase() + ")" : "";
                setText(item.getFullName() + suffix);
            }
        });
        return comboBox;
    }

    private void refreshAvailableCouriers() {
        availableCouriers.setAll(context.getCouriers().stream()
                .filter(courier -> courier.getStatus() == CourierStatus.AVAILABLE)
                .collect(Collectors.toList()));
    }

    private String resolveClientName(Integer clientId) {
        Client client = findClientById(clientId);
        return client == null ? "" : client.getFullName();
    }

    private String resolveCourierName(Integer courierId) {
        Courier courier = findCourierById(courierId);
        return courier == null ? "" : courier.getFullName();
    }

    private Client findClientById(Integer clientId) {
        if (clientId == null) {
            return null;
        }
        return context.getClients().stream()
                .filter(client -> clientId.equals(client.getId()))
                .findFirst()
                .orElse(null);
    }

    private Courier findCourierById(Integer courierId) {
        if (courierId == null) {
            return null;
        }
        return context.getCouriers().stream()
                .filter(courier -> courierId.equals(courier.getId()))
                .findFirst()
                .orElse(null);
    }

    private void deleteOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is not selected");
        }
        if (!confirmDelete("order", order.getId())) {
            return;
        }
        context.getOrderService().deleteById(order.getId());
    }

    private String formatStatus(OrderStatus status) {
        if (status == null) {
            return "";
        }
        String normalized = status.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String statusClass(OrderStatus status) {
        switch (status) {
            case DELIVERED:
                return "status-delivered";
            case IN_DELIVERY:
                return "status-in-delivery";
            case CANCELLED:
                return "status-cancelled";
            case ACCEPTED:
                return "status-accepted";
            case PREPARING:
                return "status-preparing";
            default:
                return "status-created";
        }
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
}
