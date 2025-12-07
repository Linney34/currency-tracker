package com.example.currencytracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CurrencyRateDTO {

    private String currency;
    private BigDecimal rate;
    private LocalDateTime timestamp;

    public CurrencyRateDTO() {
    }

    public CurrencyRateDTO(String currency, BigDecimal rate, LocalDateTime timestamp) {
        this.currency = currency;
        this.rate = rate;
        this.timestamp = timestamp;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String currency;
        private BigDecimal rate;
        private LocalDateTime timestamp;

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public CurrencyRateDTO build() {
            return new CurrencyRateDTO(currency, rate, timestamp);
        }
    }
}