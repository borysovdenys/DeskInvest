package com.borysov.dev.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private UUID uuid;

    private String url;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateTrack;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lastDateUpdate;

    private BigDecimal startPriceUSD;

}
