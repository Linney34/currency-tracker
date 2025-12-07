package com.example.currencytracker.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyRateDTO {
    private String currency;
    private BigDecimal rate;
    private LocalDateTime timestamp;
}