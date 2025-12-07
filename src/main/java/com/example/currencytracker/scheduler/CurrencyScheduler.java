package com.example.currencytracker.scheduler;

import com.example.currencytracker.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrencyScheduler {

    private final CurrencyService currencyService;

    @Scheduled(cron = "${scheduler.fetch-interval-cron}")
    public void fetchRates() {
        currencyService.fetchLatestRates();
    }
}