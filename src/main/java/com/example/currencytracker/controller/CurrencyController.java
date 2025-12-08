package com.example.currencytracker.controller;

import com.example.currencytracker.dto.CurrencyRateDTO;
import com.example.currencytracker.entity.CurrencyRate;
import com.example.currencytracker.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rates")
@Tag(name = "Currency API", description = "Operations for currency tracking")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService service;

    @GetMapping("/latest")
    @Operation(summary = "Get latest currency rate")
    public CurrencyRateDTO getLatest(@RequestParam String currency) {
        CurrencyRate rate = service.getLatestRate(currency);
        return CurrencyRateDTO.builder()
                .currency(rate.getCurrency())
                .rate(rate.getRate())
                .timestamp(rate.getTimestamp())
                .build();
    }

    @GetMapping("/history")
    @Operation(summary = "Get currency rate history")
    public List<CurrencyRateDTO> getHistory(
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.getHistory(currency, from, to).stream()
                .map(rate -> CurrencyRateDTO.builder()
                        .currency(rate.getCurrency())
                        .rate(rate.getRate())
                        .timestamp(rate.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/avg")
    @Operation(summary = "Get average rate")
    public BigDecimal getAverage(@RequestParam String currency, @RequestParam int period) {
        return service.getAverage(currency, period);
    }

    @GetMapping("/trend")
    @Operation(summary = "Get currency trend")
    public String getTrend(@RequestParam String currency) {
        return service.getTrend(currency);
    }
}
