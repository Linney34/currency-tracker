package com.example.currencytracker.service;

import com.example.currencytracker.dto.CurrencyRateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://api.frankfurter.app";

    /**
     * Fetch latest currency rate dynamically
     */
    public CurrencyRateDTO fetchLatestRate(String currency) {
        try {
            String url = BASE_URL + "/latest?from=" + currency + "&to=PLN";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("rates")) {
                throw new RuntimeException("No rates found for " + currency);
            }

            Map<String, Double> rates = (Map<String, Double>) response.get("rates");
            BigDecimal rate = BigDecimal.valueOf(rates.get("PLN"));
            String dateStr = (String) response.get("date");

            return CurrencyRateDTO.builder()
                    .currency(currency)
                    .rate(rate)
                    .timestamp(LocalDate.parse(dateStr).atStartOfDay())
                    .build();

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch latest rate for " + currency, e);
        }
    }

    /**
     * Fetch historical rates for a period
     */
    public List<CurrencyRateDTO> fetchHistory(String currency, LocalDate from, LocalDate to) {
        try {
            String url = BASE_URL + "/" + from + ".." + to + "?from=" + currency + "&to=PLN";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("rates")) {
                throw new RuntimeException("No historical rates found for " + currency);
            }

            Map<String, Map<String, Double>> rates = (Map<String, Map<String, Double>>) response.get("rates");
            List<CurrencyRateDTO> history = new ArrayList<>();

            for (Map.Entry<String, Map<String, Double>> entry : rates.entrySet()) {
                LocalDate date = LocalDate.parse(entry.getKey());
                BigDecimal rate = BigDecimal.valueOf(entry.getValue().get("PLN"));

                history.add(CurrencyRateDTO.builder()
                        .currency(currency)
                        .rate(rate)
                        .timestamp(date.atStartOfDay())
                        .build());
            }

            // Sort by date ascending
            history.sort(Comparator.comparing(CurrencyRateDTO::getTimestamp));
            return history;

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch history for " + currency, e);
        }
    }
}