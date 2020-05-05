package com.borysov.dev.repositories;

import com.borysov.dev.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    Optional<Item> findByUuid(UUID uuid);

    void deleteByUuid(UUID uuid);
}
