package org.example.food_delivery.model.user;

public class Client extends User {
    private String email;
    private String address;

    public Client() {
    }

    public Client(Integer id, String fullName, String phone, String email, String address) {
        super(id, fullName, phone);
        this.email = email;
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

