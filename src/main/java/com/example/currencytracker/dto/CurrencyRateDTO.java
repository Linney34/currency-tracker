package com.example.currencytracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CurrencyRateDTO {
    private String currency;
    private BigDecimal rate;
    private LocalDateTime timestamp;
}