package com.example.currencytracker.scheduler;

import com.example.currencytracker.service.CurrencyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CurrencyScheduler {

    private final CurrencyService currencyService;

    public CurrencyScheduler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Scheduled(cron = "${scheduler.fetch-interval-cron}")
    public void fetchRates() {
        currencyService.fetchLatestRates();
    }
}