package com.example.currencytracker.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_rates")
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public CurrencyRate() {
    }

    public CurrencyRate(Long id, String currency, BigDecimal rate, LocalDateTime timestamp) {
        this.id = id;
        this.currency = currency;
        this.rate = rate;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public String getCurrency() { return currency; }
    public BigDecimal getRate() { return rate; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setId(Long id) { this.id = id; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String currency;
        private BigDecimal rate;
        private LocalDateTime timestamp;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder rate(BigDecimal rate) { this.rate = rate; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public CurrencyRate build() {
            return new CurrencyRate(id, currency, rate, timestamp);
        }
    }
}