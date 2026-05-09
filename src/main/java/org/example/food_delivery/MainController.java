package org.example.food_delivery;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.food_delivery.controllers.AppContext;
import org.example.food_delivery.controllers.ThemeManager;
import org.example.food_delivery.controllers.ClientsController;
import org.example.food_delivery.controllers.CouriersController;
import org.example.food_delivery.controllers.DashboardController;
import org.example.food_delivery.controllers.OrdersController;
import org.example.food_delivery.repository.ClientRepository;
import org.example.food_delivery.repository.CourierRepository;
import org.example.food_delivery.repository.OrderRepository;
import org.example.food_delivery.service.ClientService;
import org.example.food_delivery.service.CourierService;
import org.example.food_delivery.service.OrderService;

public class MainController {
    private String initialSection = "dashboard";

    @FXML
    private BorderPane appRoot;
    @FXML
    private Button themeToggleButton;
    @FXML
    private StackPane contentStack;
    @FXML
    private VBox dashboardView;
    @FXML
    private VBox clientsView;
    @FXML
    private VBox couriersView;
    @FXML
    private VBox ordersView;
    @FXML
    private Button dashboardNavButton;
    @FXML
    private Button clientsNavButton;
    @FXML
    private Button couriersNavButton;
    @FXML
    private Button ordersNavButton;
    @FXML
    private DashboardController dashboardTabContentController;
    @FXML
    private ClientsController clientsTabContentController;
    @FXML
    private CouriersController couriersTabContentController;
    @FXML
    private OrdersController ordersTabContentController;

    @FXML
    public void initialize() {
        AppContext context = new AppContext(
                new ClientService(new ClientRepository()),
                new CourierService(new CourierRepository()),
                new OrderService(new OrderRepository())
        );
        safeInit("dashboard", () -> dashboardTabContentController.init(context));
        safeInit("clients",   () -> clientsTabContentController.init(context));
        safeInit("couriers",  () -> couriersTabContentController.init(context));
        safeInit("orders",    () -> ordersTabContentController.init(context));
        showSection(resolveSection(initialSection));
    }

    private void safeInit(String section, Runnable initCall) {
        try {
            initCall.run();
        } catch (Throwable ex) {
            System.err.println("MainController: failed to initialise " + section + " — " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void setInitialSection(String sectionKey) {
        if (sectionKey != null && !sectionKey.trim().isEmpty()) {
            this.initialSection = sectionKey.trim().toLowerCase();
        }
        if (dashboardView != null) {
            showSection(resolveSection(this.initialSection));
        }
    }

    @FXML
    private void onOpenDashboard() {
        showSection(dashboardView);
    }

    @FXML
    private void onToggleTheme() {
        if (appRoot == null) {
            return;
        }
        ThemeManager.setDark(!ThemeManager.isDark());
        ThemeManager.applyTo(appRoot);
        if (themeToggleButton != null) {
            themeToggleButton.setText(ThemeManager.isDark() ? "☀  Light" : "🌙  Dark");
        }
    }

    @FXML
    private void onOpenClients() {
        showSection(clientsView);
    }

    @FXML
    private void onOpenCouriers() {
        showSection(couriersView);
    }

    @FXML
    private void onOpenOrders() {
        showSection(ordersView);
    }

    private void showSection(VBox section) {
        dashboardView.setVisible(section == dashboardView);
        dashboardView.setManaged(section == dashboardView);
        clientsView.setVisible(section == clientsView);
        clientsView.setManaged(section == clientsView);
        couriersView.setVisible(section == couriersView);
        couriersView.setManaged(section == couriersView);
        ordersView.setVisible(section == ordersView);
        ordersView.setManaged(section == ordersView);
        updateNavState(section);
    }

    private VBox resolveSection(String sectionKey) {
        if ("clients".equals(sectionKey)) {
            return clientsView;
        }
        if ("couriers".equals(sectionKey)) {
            return couriersView;
        }
        if ("orders".equals(sectionKey)) {
            return ordersView;
        }
        return dashboardView;
    }

    private void updateNavState(VBox activeSection) {
        dashboardNavButton.getStyleClass().remove("nav-button-active");
        clientsNavButton.getStyleClass().remove("nav-button-active");
        couriersNavButton.getStyleClass().remove("nav-button-active");
        ordersNavButton.getStyleClass().remove("nav-button-active");

        if (activeSection == dashboardView) {
            dashboardNavButton.getStyleClass().add("nav-button-active");
        } else if (activeSection == clientsView) {
            clientsNavButton.getStyleClass().add("nav-button-active");
        } else if (activeSection == couriersView) {
            couriersNavButton.getStyleClass().add("nav-button-active");
        } else if (activeSection == ordersView) {
            ordersNavButton.getStyleClass().add("nav-button-active");
        }
    }
}
