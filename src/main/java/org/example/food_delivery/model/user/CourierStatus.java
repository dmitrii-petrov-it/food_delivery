package org.example.food_delivery.model.user;

public enum CourierStatus {
    AVAILABLE("available"),
    BUSY("busy"),
    INACTIVE("inactive");

    private final String dbValue;

    CourierStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static CourierStatus fromDbValue(String value) {
        for (CourierStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown courier status: " + value);
    }
}

