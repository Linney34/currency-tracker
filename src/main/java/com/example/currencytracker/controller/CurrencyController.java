package com.example.currencytracker.controller;

import com.example.currencytracker.dto.CurrencyRateDTO;
import com.example.currencytracker.enums.CurrencyCode;
import com.example.currencytracker.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/latest")
    public CurrencyRateDTO getLatest(@RequestParam CurrencyCode currency) {
        return currencyService.fetchDailyRate(currency);
    }

    @GetMapping("/history")
    public List<CurrencyRateDTO> getHistory(@RequestParam CurrencyCode currency,
                                            @RequestParam String from,
                                            @RequestParam String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        return currencyService.fetchHistory(currency, fromDate, toDate);
    }

    @GetMapping("/avg")
    public Map<String , Object> getAverage(@RequestParam CurrencyCode currency,
                                           @RequestParam int days) {
        double average = currencyService.getAverageRate(days, currency);

        return Map.ofEntries(
                Map.entry("currency", currency),
                Map.entry("avg", average)
        );
    }

    @GetMapping("/trend")
    public Map<String, Object> getTrend(@RequestParam CurrencyCode currency,
                                        @RequestParam int days) {
        String trend = currencyService.getTrend(days, currency);
        return Map.ofEntries(
                Map.entry("currency", currency),
                Map.entry("trend", trend)
        );
    }
}