package org.example.food_delivery.model.user;

public class Courier extends User {
    private CourierVehicleType vehicleType;
    private CourierStatus status;
    private byte[] photo;

    public Courier() {
    }

    public Courier(Integer id, String fullName, String phone, CourierVehicleType vehicleType, CourierStatus status) {
        super(id, fullName, phone);
        this.vehicleType = vehicleType;
        this.status = status;
    }

    public Courier(Integer id, String fullName, String phone, CourierVehicleType vehicleType, CourierStatus status, byte[] photo) {
        this(id, fullName, phone, vehicleType, status);
        this.photo = photo;
    }

    public CourierVehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(CourierVehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public CourierStatus getStatus() {
        return status;
    }

    public void setStatus(CourierStatus status) {
        this.status = status;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }
}
