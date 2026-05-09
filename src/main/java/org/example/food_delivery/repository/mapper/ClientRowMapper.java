package org.example.food_delivery.repository.mapper;

import org.example.food_delivery.model.user.Client;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientRowMapper {
    public Client mapRow(ResultSet resultSet) throws SQLException {
        return new Client(
                resultSet.getInt("id"),
                resultSet.getString("full_name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getString("address")
        );
    }
}

