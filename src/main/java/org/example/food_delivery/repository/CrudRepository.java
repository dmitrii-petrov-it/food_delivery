package org.example.food_delivery.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {
    T create(T entity);

    boolean update(T entity);

    boolean deleteById(ID id);

    Optional<T> findById(ID id);

    List<T> findAll();
}

