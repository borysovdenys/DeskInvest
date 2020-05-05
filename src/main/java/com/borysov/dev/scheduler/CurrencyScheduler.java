package com.borysov.dev.scheduler;

import com.borysov.dev.services.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class CurrencyScheduler {

    private final CurrencyService currencyService;

    //@Scheduled(cron = "1 * * * * *")
    @Scheduled(cron = "0 10 0 * * *")
    public void scheduleDailyCurrencyUpdateTask() {
        currencyService.dailyCurrencyUpdate();
    }
}