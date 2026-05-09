package org.example.food_delivery;

import org.example.food_delivery.model.order.Order;
import org.example.food_delivery.model.order.OrderStatus;
import org.example.food_delivery.model.user.Client;
import org.example.food_delivery.model.user.Courier;
import org.example.food_delivery.model.user.CourierStatus;
import org.example.food_delivery.model.user.CourierVehicleType;
import org.example.food_delivery.repository.ClientRepository;
import org.example.food_delivery.repository.CourierRepository;
import org.example.food_delivery.repository.OrderRepository;
import org.example.food_delivery.service.ClientService;
import org.example.food_delivery.service.CourierService;
import org.example.food_delivery.service.OrderService;

import java.math.BigDecimal;

public class AppRunner {
    public static void main(String[] args) {
        ClientService clientService = new ClientService(new ClientRepository());
        CourierService courierService = new CourierService(new CourierRepository());
        OrderService orderService = new OrderService(new OrderRepository());

        Client client = new Client(null, "Alex Johnson", "+1-555-0100", "alex@example.com", "Main Street 1");
        clientService.create(client);

        Courier courier = new Courier(null, "Sam Courier", "+1-555-0200", CourierVehicleType.BIKE, CourierStatus.AVAILABLE);
        courierService.create(courier);

        Order order = new Order();
        order.setClientId(client.getId());
        order.setCourierId(courier.getId());
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setRestaurantName("Burger Place");
        order.setFoodDescription("Cheeseburger, fries");
        order.setDeliveryAddress(client.getAddress());
        order.setOrderPrice(new BigDecimal("12.50"));
        order.setDeliveryFee(new BigDecimal("3.00"));
        order.setStatus(OrderStatus.CREATED);

        orderService.create(order);

        System.out.println("Created client id=" + client.getId());
        System.out.println("Created courier id=" + courier.getId());
        System.out.println("Created order id=" + order.getId());
        System.out.println("Total orders=" + orderService.findAll().size());
    }
}
