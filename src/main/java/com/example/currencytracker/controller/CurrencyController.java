package com.example.currencytracker.controller;

import com.example.currencytracker.dto.CurrencyRateDTO;
import com.example.currencytracker.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rates")
@Tag(name = "Currency API", description = "Operations for currency tracking")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService service;

    @GetMapping("/latest")
    @Operation(summary = "Get latest currency rate dynamically")
    public CurrencyRateDTO getLatest(@RequestParam String currency) {
        return service.fetchLatestRate(currency);
    }

    @GetMapping("/history")
    @Operation(summary = "Get currency rate history dynamically")
    public List<CurrencyRateDTO> getHistory(
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.fetchHistory(currency, from, to);
    }
}