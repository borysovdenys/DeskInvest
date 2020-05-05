package com.borysov.dev.models;

import com.borysov.dev.models.base.IndexEntity;
import com.borysov.dev.models.enums.CurrencyEnum;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "item")
@ToString(exclude = "item")
@Entity
@Table(name = "prices")
public class Price extends IndexEntity {

    @ManyToOne
    @JoinColumn(name="item_uuid", nullable=false)
    private Item item;

    @Enumerated(EnumType.STRING)
    private CurrencyEnum abbreviation;

    private BigDecimal startRate;

    private BigDecimal currentRate;

    public String getStartRateStr() {
        return getAbbreviation().name() + ": " + getStartRate();
    }

    public String getCurrentRateStr() {
        return getAbbreviation().name() + ": " + getCurrentRate();
    }

    public Price(CurrencyEnum abbreviation) {
        this.abbreviation = abbreviation;
    }
}
