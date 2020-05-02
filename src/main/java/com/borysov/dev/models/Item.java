package com.borysov.dev.models;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    private String url;

    private String name;

    private String picture;

    private LocalDateTime startDateTrack;

    private LocalDateTime lastDateUpdate;

    @OneToMany(mappedBy="item", cascade = CascadeType.ALL)
    private Set<Price> prices = new HashSet<>();

}
