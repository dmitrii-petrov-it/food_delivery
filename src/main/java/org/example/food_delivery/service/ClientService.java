package org.example.food_delivery.service;

import org.example.food_delivery.model.user.Client;
import org.example.food_delivery.repository.CrudRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClientService {
    private final CrudRepository<Client, Integer> repository;

    public ClientService(CrudRepository<Client, Integer> repository) {
        this.repository = repository;
    }

    public Client create(Client client) {
        validateClient(client);
        return repository.create(client);
    }

    public boolean update(Client client) {
        Objects.requireNonNull(client.getId(), "Client id is required");
        validateClient(client);
        return repository.update(client);
    }

    public boolean deleteById(Integer id) {
        Objects.requireNonNull(id, "Client id is required");
        return repository.deleteById(id);
    }

    public Optional<Client> findById(Integer id) {
        Objects.requireNonNull(id, "Client id is required");
        return repository.findById(id);
    }

    public List<Client> findAll() {
        return repository.findAll();
    }

    private void validateClient(Client client) {
        Objects.requireNonNull(client, "Client is required");
        requireText(client.getFullName(), "Full name is required");
        requireText(client.getPhone(), "Phone is required");
        requireText(client.getAddress(), "Address is required");
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}

