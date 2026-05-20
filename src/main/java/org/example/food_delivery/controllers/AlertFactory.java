package org.example.food_delivery.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;

import java.util.Optional;

/**
 * One-stop factory for the app's styled alerts. Each helper returns a fully
 * decorated {@link Alert} — gradient header per type, large emoji icon,
 * rounded card, soft shadow. UI code should call these methods instead of
 * constructing {@link Alert} instances by hand.
 */
public final class AlertFactory {

    private AlertFactory() {}

    /** Red error alert with a ❌ icon. */
    public static void showError(String header, String message) {
        build(AlertType.ERROR, "Error", header, message, "app-alert-error", "✕").showAndWait();
    }

    /** Orange/red brand info alert with an ℹ icon. */
    public static void showInfo(String header, String message) {
        build(AlertType.INFORMATION, "Info", header, message, "app-alert-info", "i").showAndWait();
    }

    /** Green success alert with a ✓ icon. */
    public static void showSuccess(String header, String message) {
        build(AlertType.INFORMATION, "Success", header, message, "app-alert-success", "✓").showAndWait();
    }

    /** Orange warning confirmation. Returns true if the user clicked "Yes". */
    public static boolean confirm(String header, String message) {
        Alert alert = build(AlertType.CONFIRMATION, "Confirm", header, message, "app-alert-warn", "!");
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, cancel);
        Button yesButton = (Button) alert.getDialogPane().lookupButton(yes);
        if (yesButton != null) {
            yesButton.getStyleClass().add("danger-button");
        }
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yes;
    }

    /** Convenience for delete confirmations. */
    public static boolean confirmDelete(String entityName) {
        return confirm("Delete " + entityName + "?",
                "This action cannot be undone. Are you sure you want to delete this " + entityName + "?");
    }

    private static Alert build(AlertType type, String title, String header, String message,
                               String typeStyleClass, String iconGlyph) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header == null ? "" : header);
        alert.setContentText(message == null ? "" : message);

        // Large emoji-style icon on the left of the header
        Label icon = new Label(iconGlyph);
        icon.getStyleClass().add("app-alert-icon");
        alert.setGraphic(icon);

        DialogPane pane = alert.getDialogPane();
        pane.getStyleClass().addAll("app-alert", typeStyleClass);
        // ThemeManager wires the stylesheet and the dark-theme class
        try {
            ThemeManager.applyTo(pane);
        } catch (Throwable ignored) {
            // theme application is best-effort — the alert still works without it
        }
        return alert;
    }
}
