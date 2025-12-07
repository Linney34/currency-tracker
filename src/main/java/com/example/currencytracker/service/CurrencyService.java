package com.example.currencytracker.service;

import com.example.currencytracker.entity.CurrencyRate;
import com.example.currencytracker.repository.CurrencyRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRateRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String API_URL = "https://api.nbp.pl/api/exchangerates/rates/A/";


    public void fetchLatestRates() {
        List<String> currencies = List.of("USD", "EUR", "PLN");
        LocalDateTime now = LocalDateTime.now();

        for (String currency : currencies) {
            try {
                Map<String, Object> response = restTemplate.getForObject(API_URL + currency + "/?format=json", Map.class);
                Map<String, Object> rateInfo = ((List<Map<String, Object>>) response.get("rates")).get(0);
                BigDecimal rate = new BigDecimal(rateInfo.get("mid").toString());

                CurrencyRate currencyRate = CurrencyRate.builder()
                        .currency(currency)
                        .rate(rate)
                        .timestamp(now)
                        .build();

                repository.save(currencyRate);
            } catch (Exception e) {
                System.err.println("Error fetching rate for " + currency + ": " + e.getMessage());
            }
        }
    }

    /**
     * Get latest cached rate
     */
    @Cacheable(value = "latestRate", key = "#currency")
    public CurrencyRate getLatestRate(String currency) {
        return repository.findTopByCurrencyOrderByTimestampDesc(currency);
    }

    /**
     * Get rate history
     */
    public List<CurrencyRate> getHistory(String currency, LocalDate from, LocalDate to) {
        return repository.findByCurrencyAndTimestampBetween(currency, from.atStartOfDay(), to.atTime(23, 59, 59));
    }

    /**
     * Get average rate for last N days
     */
    public BigDecimal getAverage(String currency, int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        LocalDate to = LocalDate.now();
        List<CurrencyRate> rates = getHistory(currency, from, to);
        return rates.stream()
                .map(CurrencyRate::getRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rates.size()), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Get trend: up / down / stable
     */
    public String getTrend(String currency) {
        List<CurrencyRate> rates = repository.findByCurrencyAndTimestampBetween(currency, LocalDateTime.now().minusDays(7), LocalDateTime.now());
        if (rates.size() < 2) return "stable";
        BigDecimal first = rates.get(0).getRate();
        BigDecimal last = rates.get(rates.size() - 1).getRate();
        int cmp = last.compareTo(first);
        if (cmp > 0) return "up";
        if (cmp < 0) return "down";
        return "stable";
    }
}