package org.example.food_delivery.model.order;

public enum OrderStatus {
    CREATED("created"),
    ACCEPTED("accepted"),
    PREPARING("preparing"),
    IN_DELIVERY("in_delivery"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    private final String dbValue;

    OrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static OrderStatus fromDbValue(String value) {
        for (OrderStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + value);
    }
}

