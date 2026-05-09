package org.example.food_delivery.service;

import org.example.food_delivery.model.user.Courier;
import org.example.food_delivery.model.user.CourierStatus;
import org.example.food_delivery.repository.CrudRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CourierService {
    private final CrudRepository<Courier, Integer> repository;

    public CourierService(CrudRepository<Courier, Integer> repository) {
        this.repository = repository;
    }

    public Optional<Courier> updateStatus(Integer courierId, CourierStatus status) {
        if (courierId == null || status == null) return Optional.empty();
        Optional<Courier> existing = repository.findById(courierId);
        if (!existing.isPresent()) return Optional.empty();
        Courier c = existing.get();
        if (c.getStatus() == status) return Optional.of(c);
        c.setStatus(status);
        repository.update(c);
        return Optional.of(c);
    }

    public Courier create(Courier courier) {
        validateCourier(courier);
        return repository.create(courier);
    }

    public boolean update(Courier courier) {
        Objects.requireNonNull(courier.getId(), "Courier id is required");
        validateCourier(courier);
        return repository.update(courier);
    }

    public boolean deleteById(Integer id) {
        Objects.requireNonNull(id, "Courier id is required");
        return repository.deleteById(id);
    }

    public Optional<Courier> findById(Integer id) {
        Objects.requireNonNull(id, "Courier id is required");
        return repository.findById(id);
    }

    public List<Courier> findAll() {
        return repository.findAll();
    }

    private void validateCourier(Courier courier) {
        Objects.requireNonNull(courier, "Courier is required");
        requireText(courier.getFullName(), "Full name is required");
        requireText(courier.getPhone(), "Phone is required");
        Objects.requireNonNull(courier.getVehicleType(), "Vehicle type is required");
        if (courier.getStatus() == null) {
            courier.setStatus(CourierStatus.AVAILABLE);
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
