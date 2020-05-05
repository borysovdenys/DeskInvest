package com.borysov.dev.repositories;

import com.borysov.dev.models.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, UUID> {

    @Query(value = "SELECT * FROM currencies c ORDER BY date DESC LIMIT :currenciesAmount", nativeQuery=true)
    List<Currency> getLatestCurrencies(@Param("currenciesAmount") int currenciesAmount);

}