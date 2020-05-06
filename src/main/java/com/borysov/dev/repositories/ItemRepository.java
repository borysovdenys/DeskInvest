package com.borysov.dev.repositories;

import com.borysov.dev.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    Optional<Item> findByUuid(UUID uuid);

    void deleteByUuid(UUID uuid);

    Page<Item> findAllByUserUuid(Pageable pageable, UUID uuid);

    List<Item> findAllByUserUuid(UUID uuid);
}
