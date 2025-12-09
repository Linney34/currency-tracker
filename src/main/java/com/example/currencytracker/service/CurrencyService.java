package com.example.currencytracker.service;

import com.example.currencytracker.dto.CurrencyRateDTO;
import com.example.currencytracker.enums.CurrencyCode;
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
    public CurrencyRateDTO fetchDailyRate(CurrencyCode currency) {
        try {
            String currencyStr = currency.name();

            String url = BASE_URL + "/latest?from=" + currencyStr + "&to=PLN";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("rates")) {
                throw new RuntimeException("No rates found for " + currencyStr);
            }

            Map<String, Double> rates = (Map<String, Double>) response.get("rates");
            BigDecimal rate = BigDecimal.valueOf(rates.get("PLN"));
            String dateStr = (String) response.get("date");

            return CurrencyRateDTO.builder()
                    .currency(currencyStr)
                    .rate(rate)
                    .timestamp(LocalDate.parse(dateStr).atTime(16, 00))
                    .build();

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch latest rate for " + currency, e);
        }
    }

    /**
     * Fetch historical rates for a period
     */
    public List<CurrencyRateDTO> fetchHistory(CurrencyCode currency, LocalDate from, LocalDate to) {
        try {
            String currencyStr = currency.name();

            String url = BASE_URL + "/" + from + ".." + to
                    + "?from=" + currencyStr + "&to=PLN";

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("rates")) {
                throw new RuntimeException("No historical rates found for " + currencyStr);
            }

            Map<String, Map<String, Double>> rates =
                    (Map<String, Map<String, Double>>) response.get("rates");

            List<CurrencyRateDTO> history = new ArrayList<>();

            for (Map.Entry<String, Map<String, Double>> entry : rates.entrySet()) {
                LocalDate date = LocalDate.parse(entry.getKey());
                BigDecimal rate = BigDecimal.valueOf(entry.getValue().get("PLN"));

                history.add(CurrencyRateDTO.builder()
                        .currency(currencyStr)
                        .rate(rate)
                        .timestamp(date.atTime(16,00))
                        .build());
            }

            history.sort(Comparator.comparing(CurrencyRateDTO::getTimestamp));
            return history;

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch history for " + currency, e);
        }
    }

    public double getAverageRate(int days, CurrencyCode currency) {
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = LocalDate.now().minusDays(days);

        List<CurrencyRateDTO> rates = fetchHistory(currency, fromDate, toDate);
        return rates.stream()
                .mapToDouble(rate -> rate.getRate().doubleValue())
                .average()
                .orElse(0.0);
    }

    public String getTrend(int days, CurrencyCode currency){
        LocalDate from = LocalDate.now().minusDays(days);
        LocalDate to = LocalDate.now();

        List<CurrencyRateDTO> rates;
        try {
            rates = fetchHistory(currency, from, to);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch history for " + currency, e);
        }

        if (rates == null || rates.size() < 2) return "stable";

        double trendScore = 0;
        for (int i = 1; i<rates.size(); i++){
            trendScore += rates.get(i).getRate().doubleValue() - rates.get(i-1).getRate().doubleValue();
        }

        if (trendScore > 0) return "up";
        if(trendScore < 0) return "down";
        return "stable";
    }
}