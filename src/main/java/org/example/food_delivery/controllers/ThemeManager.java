package org.example.food_delivery.controllers;

import javafx.scene.Parent;
import javafx.scene.control.DialogPane;

public final class ThemeManager {
    public static final String DARK_CLASS = "dark-theme";
    private static final String CSS_PATH = "/org/example/food_delivery/app.css";

    private static boolean dark = false;

    private ThemeManager() {}

    public static boolean isDark() {
        return dark;
    }

    public static void setDark(boolean value) {
        dark = value;
    }

    public static void applyTo(Parent root) {
        if (root == null) return;
        if (!root.getStylesheets().contains(cssUrl())) {
            root.getStylesheets().add(cssUrl());
        }
        if (dark) {
            if (!root.getStyleClass().contains(DARK_CLASS)) {
                root.getStyleClass().add(DARK_CLASS);
            }
        } else {
            root.getStyleClass().remove(DARK_CLASS);
        }
    }

    public static void applyTo(DialogPane pane) {
        if (pane == null) return;
        if (!pane.getStylesheets().contains(cssUrl())) {
            pane.getStylesheets().add(cssUrl());
        }
        if (dark) {
            if (!pane.getStyleClass().contains(DARK_CLASS)) {
                pane.getStyleClass().add(DARK_CLASS);
            }
        } else {
            pane.getStyleClass().remove(DARK_CLASS);
        }
    }

    private static String cssUrl() {
        return ThemeManager.class.getResource(CSS_PATH).toExternalForm();
    }
}
