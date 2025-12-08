package com.example.currencytracker.schedulerTest;

import com.example.currencytracker.scheduler.CurrencyScheduler;
import com.example.currencytracker.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@SpringBootTest
class CurrencySchedulerTest {

    @Autowired
    private CurrencyScheduler scheduler;

    @MockitoBean
    private CurrencyService service;

    @Test
    void testFetchRates() {
        scheduler.fetchRates();
        Mockito.verify(service).fetchLatestRates();
    }
}