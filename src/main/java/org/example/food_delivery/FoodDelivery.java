package org.example.food_delivery;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

public class FoodDelivery extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("FoodFlow — Delivery Console");
        if (openMainMenu(stage)) {
            stage.setMaximized(true);
            stage.show();
        }
    }

    /** Loads the main menu into the given stage. Returns false on failure. */
    public static boolean openMainMenu(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(FoodDelivery.class.getResource("main-menu.fxml"));
            Parent root = loader.load();
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root, 1280, 820);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            stage.setTitle("FoodFlow — Delivery Console");
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            showLoadError("Unable to open main menu", ex);
            return false;
        }
    }

    /** Loads the workspace and selects the requested section. Returns false on failure. */
    public static boolean openWorkspace(Stage stage, String section) {
        try {
            FXMLLoader loader = new FXMLLoader(FoodDelivery.class.getResource("hello-view.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setInitialSection(section);
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root, 1280, 820);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            stage.setTitle("FoodFlow — Delivery Console");
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            showLoadError("Unable to open the " + section + " page", ex);
            return false;
        }
    }

    private static void showLoadError(String header, Throwable ex) {
        try {
            String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.setTitle("FoodFlow");
            alert.setHeaderText(header);
            DialogPane pane = alert.getDialogPane();
            try {
                pane.getStylesheets().add(FoodDelivery.class.getResource("app.css").toExternalForm());
                pane.getStyleClass().add("app-dialog");
            } catch (Throwable ignored) {
                // styling is best-effort
            }
            alert.showAndWait();
        } catch (Throwable ignored) {
            // last-resort: don't escalate UI errors
        }
    }
}
