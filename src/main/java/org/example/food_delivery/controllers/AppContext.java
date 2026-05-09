package org.example.food_delivery.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.food_delivery.model.order.Order;
import org.example.food_delivery.model.user.Client;
import org.example.food_delivery.model.user.Courier;
import org.example.food_delivery.service.ClientService;
import org.example.food_delivery.service.CourierService;
import org.example.food_delivery.service.OrderService;

public class AppContext {
    private final ClientService clientService;
    private final CourierService courierService;
    private final OrderService orderService;
    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private final ObservableList<Courier> couriers = FXCollections.observableArrayList();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();

    public AppContext(ClientService clientService, CourierService courierService, OrderService orderService) {
        this.clientService = clientService;
        this.courierService = courierService;
        this.orderService = orderService;
    }

    public ClientService getClientService() {
        return clientService;
    }

    public CourierService getCourierService() {
        return courierService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public ObservableList<Client> getClients() {
        return clients;
    }

    public ObservableList<Courier> getCouriers() {
        return couriers;
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }
}

