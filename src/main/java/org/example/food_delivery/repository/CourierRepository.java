package org.example.food_delivery.repository;

import org.example.food_delivery.database.DBConnection;
import org.example.food_delivery.model.user.Courier;
import org.example.food_delivery.repository.mapper.CourierRowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourierRepository implements CrudRepository<Courier, Integer> {
    private final CourierRowMapper rowMapper = new CourierRowMapper();

    @Override
    public Courier create(Courier courier) {
        String sql = "INSERT INTO couriers (full_name, phone, vehicle_type, status, photo) VALUES (?, ?, ?, ?, ?) RETURNING id";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courier.getFullName());
            statement.setString(2, courier.getPhone());
            statement.setString(3, courier.getVehicleType().getDbValue());
            statement.setString(4, courier.getStatus().getDbValue());
            if (courier.getPhoto() == null) {
                statement.setNull(5, Types.BINARY);
            } else {
                statement.setBytes(5, courier.getPhoto());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    courier.setId(resultSet.getInt("id"));
                    return courier;
                }
            }
            throw new RepositoryException("Failed to create courier", null);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to create courier", e);
        }
    }

    @Override
    public boolean update(Courier courier) {
        String sql = "UPDATE couriers SET full_name = ?, phone = ?, vehicle_type = ?, status = ?, photo = ? WHERE id = ?";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courier.getFullName());
            statement.setString(2, courier.getPhone());
            statement.setString(3, courier.getVehicleType().getDbValue());
            statement.setString(4, courier.getStatus().getDbValue());
            if (courier.getPhoto() == null) {
                statement.setNull(5, Types.BINARY);
            } else {
                statement.setBytes(5, courier.getPhoto());
            }
            statement.setInt(6, courier.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update courier", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM couriers WHERE id = ?";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete courier", e);
        }
    }

    @Override
    public Optional<Courier> findById(Integer id) {
        String sql = "SELECT id, full_name, phone, vehicle_type, status, photo FROM couriers WHERE id = ?";
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
            throw new RepositoryException("Failed to load courier", e);
        }
    }

    @Override
    public List<Courier> findAll() {
        String sql = "SELECT id, full_name, phone, vehicle_type, status, photo FROM couriers ORDER BY id";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Courier> couriers = new ArrayList<>();
            while (resultSet.next()) {
                couriers.add(rowMapper.mapRow(resultSet));
            }
            return couriers;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load couriers", e);
        }
    }
}
