package com.borysov.dev.services.impl;

import com.borysov.dev.models.Currency;
import com.borysov.dev.models.enums.CurrencyEnum;
import com.borysov.dev.repositories.CurrencyRepository;
import com.borysov.dev.services.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository, RestTemplateBuilder restTemplateBuilder) {
        this.currencyRepository = currencyRepository;
        this.restTemplate = restTemplateBuilder.build();
    }

    private final static String OPEN_EXCHANGE_RARE_API = "https://openexchangerates.org/api/latest.json?app_id=%s";

    @Value("${openexchangerates.app_id}")
    private String appId;

    @Override
    public void dailyCurrencyUpdate() {
        JSONObject jsonObject =
                new JSONObject(restTemplate.getForObject(String.format(OPEN_EXCHANGE_RARE_API, appId), String.class));
        JSONObject rates = jsonObject.getJSONObject("rates");
        for (CurrencyEnum value : CurrencyEnum.values()) {
            Currency exchangeRate = Currency.builder()
                    .abbreviation(value)
                    .date(LocalDateTime.now())
                    .rate(rates.getBigDecimal(value.name()).setScale(2, RoundingMode.HALF_UP))
                    .build();

            currencyRepository.save(exchangeRate);
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void scheduleDailyCurrencyUpdateTask() {
        dailyCurrencyUpdate();
    }
}