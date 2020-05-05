package com.borysov.dev.models;

import com.borysov.dev.models.base.IndexEntity;
import com.borysov.dev.models.enums.CurrencyEnum;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Table(name = "currencies")
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Currency extends IndexEntity {

    @Enumerated(EnumType.STRING)
    private CurrencyEnum abbreviation;

    @NotNull
    private BigDecimal rate;

    private LocalDateTime date;

}
