package org.example.food_delivery.service;

import org.example.food_delivery.model.order.Order;
import org.example.food_delivery.model.order.OrderStatus;
import org.example.food_delivery.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order create(Order order) {
        validateOrder(order);
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.CREATED);
        }
        return repository.create(order);
    }

    public boolean update(Order order) {
        Objects.requireNonNull(order.getId(), "Order id is required");
        validateOrder(order);
        return repository.update(order);
    }

    public boolean deleteById(Integer id) {
        Objects.requireNonNull(id, "Order id is required");
        return repository.deleteById(id);
    }

    public Optional<Order> findById(Integer id) {
        Objects.requireNonNull(id, "Order id is required");
        return repository.findById(id);
    }

    public List<Order> findAll() {
        return repository.findAll();
    }

    private void validateOrder(Order order) {
        Objects.requireNonNull(order, "Order is required");
        Objects.requireNonNull(order.getClientId(), "Client id is required");
        if (order.getOrderNumber() == null || order.getOrderNumber().trim().isEmpty()) {
            order.setOrderNumber(generateOrderNumber());
        }
        requireText(order.getRestaurantName(), "Restaurant name is required");
        requireText(order.getFoodDescription(), "Food description is required");
        requireText(order.getDeliveryAddress(), "Delivery address is required");
        requireMoney(order.getOrderPrice(), "Order price is required");
        requireMoney(order.getDeliveryFee(), "Delivery fee is required");
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.CREATED);
        }
    }

    private String generateOrderNumber() {
        return String.format("FD-%04d", repository.nextOrderNumberSequence());
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireMoney(BigDecimal value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Money amount must be >= 0");
        }
    }
}
