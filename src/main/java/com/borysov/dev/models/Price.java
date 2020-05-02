package com.borysov.dev.models;

import com.borysov.dev.models.enums.CurrencyEnum;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "prices")
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name="item_uuid", nullable=false)
    private Item item;

    @Enumerated(EnumType.STRING)
    private CurrencyEnum abbreviation;

    private BigDecimal startRate;

    private BigDecimal currentRate;
}
