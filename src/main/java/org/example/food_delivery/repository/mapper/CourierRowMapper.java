package org.example.food_delivery.repository.mapper;

import org.example.food_delivery.model.user.Courier;
import org.example.food_delivery.model.user.CourierStatus;
import org.example.food_delivery.model.user.CourierVehicleType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CourierRowMapper {
    public Courier mapRow(ResultSet resultSet) throws SQLException {
        return new Courier(
                resultSet.getInt("id"),
                resultSet.getString("full_name"),
                resultSet.getString("phone"),
                CourierVehicleType.fromDbValue(resultSet.getString("vehicle_type")),
                CourierStatus.fromDbValue(resultSet.getString("status"))
        );
    }
}
