package org.example.food_delivery.repository;

import org.example.food_delivery.database.DBConnection;
import org.example.food_delivery.model.user.Client;
import org.example.food_delivery.repository.mapper.ClientRowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientRepository implements CrudRepository<Client, Integer> {
    private final ClientRowMapper rowMapper = new ClientRowMapper();

    @Override
    public Client create(Client client) {
        String sql = "INSERT INTO clients (full_name, phone, email, address) VALUES (?, ?, ?, ?) RETURNING id";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.getFullName());
            statement.setString(2, client.getPhone());
            statement.setString(3, client.getEmail());
            statement.setString(4, client.getAddress());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    client.setId(resultSet.getInt("id"));
                    return client;
                }
            }
            throw new RepositoryException("Failed to create client", null);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to create client", e);
        }
    }

    @Override
    public boolean update(Client client) {
        String sql = "UPDATE clients SET full_name = ?, phone = ?, email = ?, address = ? WHERE id = ?";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.getFullName());
            statement.setString(2, client.getPhone());
            statement.setString(3, client.getEmail());
            statement.setString(4, client.getAddress());
            statement.setInt(5, client.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update client", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete client", e);
        }
    }

    @Override
    public Optional<Client> findById(Integer id) {
        String sql = "SELECT id, full_name, phone, email, address FROM clients WHERE id = ?";
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
            throw new RepositoryException("Failed to load client", e);
        }
    }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT id, full_name, phone, email, address FROM clients ORDER BY id";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Client> clients = new ArrayList<>();
            while (resultSet.next()) {
                clients.add(rowMapper.mapRow(resultSet));
            }
            return clients;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load clients", e);
        }
    }
}
