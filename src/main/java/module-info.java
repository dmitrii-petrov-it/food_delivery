module org.example.food_delivery {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.food_delivery to javafx.fxml;
    opens org.example.food_delivery.controllers to javafx.fxml;
    opens org.example.food_delivery.model.user to javafx.base;
    opens org.example.food_delivery.model.order to javafx.base;
    exports org.example.food_delivery;
}