package org.example.food_delivery.repository;

import org.example.food_delivery.database.DBConnection;
import org.example.food_delivery.model.order.Order;
import org.example.food_delivery.model.order.OrderStatus;
import org.example.food_delivery.repository.mapper.OrderRowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrderRepository implements CrudRepository<Order, Integer> {
    private final OrderRowMapper rowMapper = new OrderRowMapper();

    @Override
    public Order create(Order order) {
        String sql = "INSERT INTO orders (client_id, courier_id, order_number, restaurant_name, food_description, "
                + "delivery_address, order_price, delivery_fee, status, delivered_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        Connection connection = DBConnection.getConnection();
        OrderStatus status = Objects.requireNonNull(order.getStatus(), "Order status is required");
        Integer clientId = Objects.requireNonNull(order.getClientId(), "Client id is required");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clientId);
            if (order.getCourierId() == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setInt(2, order.getCourierId());
            }
            statement.setString(3, order.getOrderNumber());
            statement.setString(4, order.getRestaurantName());
            statement.setString(5, order.getFoodDescription());
            statement.setString(6, order.getDeliveryAddress());
            statement.setBigDecimal(7, order.getOrderPrice());
            statement.setBigDecimal(8, order.getDeliveryFee());
            statement.setString(9, status.getDbValue());
            if (order.getDeliveredAt() == null) {
                statement.setNull(10, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(10, Timestamp.valueOf(order.getDeliveredAt()));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    order.setId(resultSet.getInt("id"));
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    if (createdAt != null) {
                        order.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    return order;
                }
            }
            throw new RepositoryException("Failed to create order", null);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to create order", e);
        }
    }

    @Override
    public boolean update(Order order) {
        String sql = "UPDATE orders SET client_id = ?, courier_id = ?, order_number = ?, restaurant_name = ?, "
                + "food_description = ?, delivery_address = ?, order_price = ?, delivery_fee = ?, status = ?, "
                + "delivered_at = ? WHERE id = ?";
        Connection connection = DBConnection.getConnection();
        Integer clientId = Objects.requireNonNull(order.getClientId(), "Client id is required");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clientId);
            if (order.getCourierId() == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setInt(2, order.getCourierId());
            }
            statement.setString(3, order.getOrderNumber());
            statement.setString(4, order.getRestaurantName());
            statement.setString(5, order.getFoodDescription());
            statement.setString(6, order.getDeliveryAddress());
            statement.setBigDecimal(7, order.getOrderPrice());
            statement.setBigDecimal(8, order.getDeliveryFee());
            statement.setString(9, order.getStatus().getDbValue());
            if (order.getDeliveredAt() == null) {
                statement.setNull(10, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(10, Timestamp.valueOf(order.getDeliveredAt()));
            }
            statement.setInt(11, order.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update order", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete order", e);
        }
    }

    @Override
    public Optional<Order> findById(Integer id) {
        String sql = "SELECT id, client_id, courier_id, order_number, restaurant_name, food_description, "
                + "delivery_address, order_price, delivery_fee, status, created_at, delivered_at "
                + "FROM orders WHERE id = ?";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(rowMapper.mapRow(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load order", e);
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT id, client_id, courier_id, order_number, restaurant_name, food_description, "
                + "delivery_address, order_price, delivery_fee, status, created_at, delivered_at "
                + "FROM orders ORDER BY id";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Order> orders = new ArrayList<>();
            while (resultSet.next()) {
                orders.add(rowMapper.mapRow(resultSet));
            }
            return orders;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load orders", e);
        }
    }
}
