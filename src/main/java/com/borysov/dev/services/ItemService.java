package com.borysov.dev.services;

import com.borysov.dev.dtos.ItemDto;
import com.borysov.dev.models.Item;
import com.borysov.dev.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.UUID;

public interface ItemService {
    ItemDto findAndPrepareItem(String id);

    boolean updateItem(ItemDto modifiedItem, User user) throws IOException;

    Page<Item> getAll(Pageable pageable);

    Item findItemByUUID(UUID fromString);

    void deleteItemByUUID(UUID uuid);

    Page<Item> getAllByUserUUID(Pageable pageable, UUID uuid);

    boolean updateItemsByUserUUID(UUID currentAuditorUUID) throws IOException;

    void deleteItemsByUserUUID(UUID currentAuditorUUID);
}
