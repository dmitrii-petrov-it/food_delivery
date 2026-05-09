package org.example.food_delivery.repository.mapper;

import org.example.food_delivery.model.order.Order;
import org.example.food_delivery.model.order.OrderStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class OrderRowMapper {
    public Order mapRow(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Timestamp deliveredAt = resultSet.getTimestamp("delivered_at");
        LocalDateTime created = createdAt == null ? null : createdAt.toLocalDateTime();
        LocalDateTime delivered = deliveredAt == null ? null : deliveredAt.toLocalDateTime();
        return new Order(
                resultSet.getInt("id"),
                resultSet.getInt("client_id"),
                resultSet.getObject("courier_id", Integer.class),
                resultSet.getString("order_number"),
                resultSet.getString("restaurant_name"),
                resultSet.getString("food_description"),
                resultSet.getString("delivery_address"),
                resultSet.getBigDecimal("order_price"),
                resultSet.getBigDecimal("delivery_fee"),
                OrderStatus.fromDbValue(resultSet.getString("status")),
                created,
                delivered
        );
    }
}

