package com.example.currencytracker.repository;

import com.example.currencytracker.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    CurrencyRate findTopByCurrencyOrderByTimestampDesc(String currency);

    List<CurrencyRate> findByCurrencyAndTimestampBetween(String currency, LocalDateTime from, LocalDateTime to);

    boolean existsByCurrencyAndTimestamp(String currency, LocalDateTime timestamp);
}