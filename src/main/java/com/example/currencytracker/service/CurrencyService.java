package com.example.currencytracker.service;

import com.example.currencytracker.entity.CurrencyRate;
import com.example.currencytracker.repository.CurrencyRateRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {

    private final CurrencyRateRepository repository;

    public CurrencyService(CurrencyRateRepository repository) {
        this.repository = repository;
    }
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_URL = "https://api.nbp.pl/api/exchangerates/rates/a/";

    private static final List<String> CURRENCIES = List.of("USD", "EUR");

    /**
     * Fetch latest USD and EUR rates vs PLN and save to DB
     */
    public void saveRate(CurrencyRate rate) {
        repository.save(rate);
    }

    public void fetchLatestRates() {
        LocalDateTime now = LocalDateTime.now();

        for (String currency : CURRENCIES) {
            try {
                Map<String, Object> response = restTemplate.getForObject(API_URL + currency + "/?format=json", Map.class);
                if (response == null || !response.containsKey("rates")) {
                    System.err.println("No rates found for " + currency);
                    continue;
                }

                List<Map<String, Object>> rates = (List<Map<String, Object>>) response.get("rates");
                if (rates.isEmpty()) {
                    System.err.println("Empty rates list for " + currency);
                    continue;
                }

                BigDecimal rate = new BigDecimal(rates.get(0).get("mid").toString());

                CurrencyRate currencyRate = CurrencyRate.builder()
                        .currency(currency)
                        .rate(rate)
                        .timestamp(now)
                        .build();

                repository.save(currencyRate);

            } catch (RestClientException e) {
                System.err.println("Error fetching rate for " + currency + ": " + e.getMessage());
            }
        }
    }

    /**
     * Get latest cached rate for a currency (USD or EUR)
     */
    @Cacheable(value = "latestRate", key = "#currency")
    public CurrencyRate getLatestRate(String currency) {
        return repository.findTopByCurrencyOrderByTimestampDesc(currency);
    }

    /**
     * Get currency history from DB
     */
    public List<CurrencyRate> getHistory(String currency, LocalDate from, LocalDate to) {
        return repository.findByCurrencyAndTimestampBetween(currency, from.atStartOfDay(), to.atTime(23, 59, 59));
    }

    /**
     * Get average rate over last N days
     */
    public BigDecimal getAverage(String currency, int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        LocalDate to = LocalDate.now();
        List<CurrencyRate> rates = getHistory(currency, from, to);

        if (rates.isEmpty()) return BigDecimal.ZERO;

        return rates.stream()
                .map(CurrencyRate::getRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rates.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Get trend for the last 7 days: up / down / stable
     */
    public String getTrend(String currency) {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();
        List<CurrencyRate> rates = repository.findByCurrencyAndTimestampBetween(currency, from, to);

        if (rates.size() < 2) return "stable";

        BigDecimal first = rates.getFirst().getRate();
        BigDecimal last = rates.getLast().getRate();

        int cmp = last.compareTo(first);
        if (cmp > 0) return "up";
        if (cmp < 0) return "down";
        return "stable";
    }
}