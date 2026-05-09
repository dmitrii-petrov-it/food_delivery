package org.example.food_delivery.controllers;

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
import org.example.food_delivery.model.user.Client;

import java.util.Optional;
import java.util.function.UnaryOperator;

public class ClientsController {
    private AppContext context;
    private FilteredList<Client> filteredClients;

    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, Integer> clientIdColumn;
    @FXML
    private TableColumn<Client, String> clientNameColumn;
    @FXML
    private TableColumn<Client, String> clientPhoneColumn;
    @FXML
    private TableColumn<Client, String> clientEmailColumn;
    @FXML
    private TableColumn<Client, String> clientAddressColumn;
    @FXML
    private TableColumn<Client, Void> clientActionsColumn;
    @FXML
    private Label totalClientsLabel;
    @FXML
    private Label clientsWithEmailLabel;
    @FXML
    private Label clientsWithAddressLabel;
    @FXML
    private TextField clientSearchField;
    @FXML
    private ComboBox<String> clientProfileFilterBox;
    @FXML
    private TextField clientAddressFilterField;
    @FXML
    private TextField clientContactFilterField;

    public void init(AppContext context) {
        this.context = context;
        setupClientTable();
        setupFilters();
        try {
            refreshClients();
        } catch (Exception ex) {
            System.err.println("ClientsController init: failed to load clients — " + ex.getMessage());
            updateStats();
        }
    }

    private void setupClientTable() {
        // show sequential index instead of DB id
        clientIdColumn.setCellFactory(col -> new TableCell<Client, Integer>() {
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
        clientNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        clientPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        clientEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        clientAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        setupActionsColumn();
        filteredClients = new FilteredList<>(context.getClients(), client -> true);
        clientTable.setItems(filteredClients);
    }

    private void setupFilters() {
        clientProfileFilterBox.getItems().setAll("All Clients", "With Email", "With Address", "Complete Profiles");
        clientProfileFilterBox.getSelectionModel().selectFirst();
        clientSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        clientProfileFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        clientAddressFilterField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        clientContactFilterField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        if (filteredClients == null) {
            return;
        }
        filteredClients.setPredicate(client -> matchesSearch(client) && matchesProfileFilter(client) && matchesAddressFilter(client) && matchesContactFilter(client));
    }

    private boolean matchesSearch(Client client) {
        String query = normalize(clientSearchField.getText());
        if (query.isEmpty()) {
            return true;
        }
        return contains(client.getFullName(), query)
                || contains(client.getPhone(), query)
                || contains(client.getEmail(), query)
                || contains(client.getAddress(), query);
    }

    private boolean matchesProfileFilter(Client client) {
        String filter = clientProfileFilterBox.getValue();
        if (filter == null || "All Clients".equals(filter)) {
            return true;
        }
        if ("With Email".equals(filter)) {
            return hasText(client.getEmail());
        }
        if ("With Address".equals(filter)) {
            return hasText(client.getAddress());
        }
        return hasText(client.getEmail()) && hasText(client.getAddress());
    }

    private boolean matchesAddressFilter(Client client) {
        String query = normalize(clientAddressFilterField.getText());
        return query.isEmpty() || contains(client.getAddress(), query);
    }

    private boolean matchesContactFilter(Client client) {
        String query = normalize(clientContactFilterField.getText());
        return query.isEmpty() || contains(client.getEmail(), query) || contains(client.getPhone(), query);
    }

    private void setupActionsColumn() {
        clientActionsColumn.setCellFactory(column -> new TableCell<Client, Void>() {
            private final Button editButton = createIconButton("\u270E", "table-edit-icon-button");
            private final Button deleteButton = createIconButton("\uD83D\uDDD1", "table-delete-icon-button");
            private final HBox box = new HBox(8.0, editButton, deleteButton);

            {
                box.setAlignment(Pos.CENTER);
                editButton.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    showClientDialog("Edit Client", "Update client profile", client)
                            .ifPresent(updated -> updateClient(client.getId(), updated));
                });
                deleteButton.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    try {
                        deleteClient(client);
                        refreshClients();
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
    private void onClientAdd() {
        showClientDialog("Add Client", "Create a new client", null).ifPresent(client -> {
            try {
                context.getClientService().create(client);
                refreshClients();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onClientRefresh() {
        clientSearchField.clear();
        clientAddressFilterField.clear();
        clientContactFilterField.clear();
        clientProfileFilterBox.getSelectionModel().selectFirst();
        refreshClients();
    }

    @FXML
    private void onBackToMainMenu(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FoodDelivery.openMainMenu(stage);
    }

    private Optional<Client> showClientDialog(String title, String header, Client existingClient) {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        styleDialog(dialog.getDialogPane());

        boolean editing = existingClient != null;
        ButtonType submitButtonType = new ButtonType(editing ? "Save Changes" : "Add Client", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField addressField = new TextField();
        phoneField.setTextFormatter(createPhoneFormatter());

        Label nameError = createErrorLabel();
        Label phoneError = createErrorLabel();
        Label emailError = createErrorLabel();
        Label addressError = createErrorLabel();

        if (editing) {
            nameField.setText(existingClient.getFullName());
            phoneField.setText(existingClient.getPhone());
            emailField.setText(existingClient.getEmail());
            addressField.setText(existingClient.getAddress());
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
        form.add(new Label("Email"), 0, row);
        form.add(emailField, 1, row++);
        form.add(emailError, 1, row++);
        form.add(new Label("Address"), 0, row);
        form.add(addressField, 1, row++);
        form.add(addressError, 1, row++);
        dialog.getDialogPane().setContent(form);

        // inline validation
        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        Runnable validate = () -> {
            boolean valid = true;
            valid &= applyFieldError(nameField, nameError, isNonEmpty(nameField.getText()), "Full name is required.");
            valid &= applyFieldError(phoneField, phoneError, isValidPhone(phoneField.getText()), "Phone must be 12 characters total: optional '+' at start and 11 digits.");
            boolean emailOk = emailField.getText() == null || emailField.getText().trim().isEmpty() || isValidEmail(emailField.getText());
            valid &= applyFieldError(emailField, emailError, emailOk, "Email format is invalid.");
            valid &= applyFieldError(addressField, addressError, isNonEmpty(addressField.getText()), "Address is required.");
            submitButton.setDisable(!valid);
        };
        nameField.textProperty().addListener((obs, o, n) -> validate.run());
        phoneField.textProperty().addListener((obs, o, n) -> validate.run());
        emailField.textProperty().addListener((obs, o, n) -> validate.run());
        addressField.textProperty().addListener((obs, o, n) -> validate.run());
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
            return new Client(editing ? existingClient.getId() : null,
                    nameField.getText(),
                    phoneField.getText(),
                    emailField.getText(),
                    addressField.getText());
        });
        return dialog.showAndWait();
    }

    private boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\+?\\d{11}");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
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
            return digits.length() <= 11 ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    private void updateClient(Integer clientId, Client updatedClient) {
        try {
            updatedClient.setId(clientId);
            context.getClientService().update(updatedClient);
            refreshClients();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshClients() {
        context.getClients().setAll(context.getClientService().findAll());
        applyFilters();
        updateStats();
    }

    private void updateStats() {
        int total = context.getClients().size();
        long withEmail = context.getClients().stream().filter(client -> hasText(client.getEmail())).count();
        long withAddress = context.getClients().stream().filter(client -> hasText(client.getAddress())).count();
        totalClientsLabel.setText(String.valueOf(total));
        clientsWithEmailLabel.setText(String.valueOf(withEmail));
        clientsWithAddressLabel.setText(String.valueOf(withAddress));
    }

    private void deleteClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client is not selected");
        }
        if (!confirmDelete("client", client.getId())) {
            return;
        }
        context.getClientService().deleteById(client.getId());
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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
}
