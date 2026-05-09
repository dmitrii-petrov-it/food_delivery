package org.example.food_delivery.model.user;

public enum CourierVehicleType {
    BIKE("bike"),
    CAR("car"),
    SCOOTER("scooter");

    private final String dbValue;

    CourierVehicleType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static CourierVehicleType fromDbValue(String value) {
        for (CourierVehicleType type : values()) {
            if (type.dbValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown courier vehicle type: " + value);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
