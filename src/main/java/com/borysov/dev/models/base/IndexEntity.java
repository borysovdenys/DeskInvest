package com.borysov.dev.models.base;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
@MappedSuperclass
public abstract class IndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, updatable = false)
    private UUID uuid;

}