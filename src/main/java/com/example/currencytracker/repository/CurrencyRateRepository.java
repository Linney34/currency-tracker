package com.example.currencytracker.repository;

import com.example.currencytracker.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    List<CurrencyRate> findByCurrencyAndTimestampBetween(String currency, LocalDateTime from, LocalDateTime to);

    CurrencyRate findTopByCurrencyOrderByTimestampDesc(String currency);
}