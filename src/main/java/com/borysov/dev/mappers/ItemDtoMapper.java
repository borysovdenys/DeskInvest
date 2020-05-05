package com.borysov.dev.mappers;

import com.borysov.dev.dtos.ItemDto;
import com.borysov.dev.models.Item;
import com.borysov.dev.models.Price;
import com.borysov.dev.models.enums.CurrencyEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class ItemDtoMapper {

/*    @Autowired
    OfficeRepository officeRepository;*/

    @Mapping(target = "startPriceUSD", source = "prices", qualifiedByName = "getStartPrice")
    @Mapping(target = "startDateTrack", source = "startDateTrack", qualifiedByName = "getStartPrice")
    @Mapping(target = "lastDateUpdate", source = "lastDateUpdate", qualifiedByName = "getStartPrice")
    public abstract ItemDto toDto(Item item);

/*    @Mapping(target = "uuid", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "offices", source = "officeUuids", qualifiedByName = "checkOfficeUuids")
    @Mapping(target = "alertDates", ignore = true)
    @Mapping(target = "periods", ignore = true)
    public abstract Tax toModel(TaxRequest taxRequest);

    @Mapping(target = "offices", source = "officeUuids", qualifiedByName = "checkOfficeUuids")
    @Mapping(target = "alertDates", ignore = true)
    @Mapping(target = "periods", ignore = true)
    public abstract Tax toEntity(TaxRequest taxRequest, @MappingTarget Tax tax);*/


    @Named("getStartPrice")
    BigDecimal getOffices(Set<Price> prices) {
        return prices.stream().filter(x -> CurrencyEnum.USD.equals(x.getAbbreviation()))
                .map(Price::getStartRate).findFirst().orElse(BigDecimal.ZERO);
    }

    @Named("getLocalDateTime")
    LocalDate getOffices(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate();
    }
}