package org.example.food_delivery.model.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private Integer id;
    private Integer clientId;
    private Integer courierId;
    private String orderNumber;
    private String restaurantName;
    private String foodDescription;
    private String deliveryAddress;
    private BigDecimal orderPrice;
    private BigDecimal deliveryFee;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;

    public Order() {
    }

    public Order(Integer id,
                 Integer clientId,
                 Integer courierId,
                 String orderNumber,
                 String restaurantName,
                 String foodDescription,
                 String deliveryAddress,
                 BigDecimal orderPrice,
                 BigDecimal deliveryFee,
                 OrderStatus status,
                 LocalDateTime createdAt,
                 LocalDateTime deliveredAt) {
        this.id = id;
        this.clientId = clientId;
        this.courierId = courierId;
        this.orderNumber = orderNumber;
        this.restaurantName = restaurantName;
        this.foodDescription = foodDescription;
        this.deliveryAddress = deliveryAddress;
        this.orderPrice = orderPrice;
        this.deliveryFee = deliveryFee;
        this.status = status;
        this.createdAt = createdAt;
        this.deliveredAt = deliveredAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getCourierId() {
        return courierId;
    }

    public void setCourierId(Integer courierId) {
        this.courierId = courierId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}

