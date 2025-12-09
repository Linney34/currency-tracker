package com.example.currencytracker.controllerTest;

import com.example.currencytracker.controller.CurrencyController;
import com.example.currencytracker.dto.CurrencyRateDTO;
import com.example.currencytracker.enums.CurrencyCode;
import com.example.currencytracker.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyControllerTest {

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyController currencyController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLatest_success() {
        CurrencyRateDTO dto = new CurrencyRateDTO("USD", BigDecimal.valueOf(4.0),
                LocalDateTime.parse("2025-01-01T00:00:00"));
        when(currencyService.fetchDailyRate(CurrencyCode.USD)).thenReturn(dto);

        CurrencyRateDTO result = currencyController.getLatest(CurrencyCode.USD);

        assertEquals("USD", result.getCurrency());
        assertEquals(BigDecimal.valueOf(4.0), result.getRate());
        assertEquals(LocalDateTime.parse("2025-01-01T00:00:00"), result.getTimestamp());
        verify(currencyService, times(1)).fetchDailyRate(CurrencyCode.USD);
    }

    @Test
    void getLatest_serviceThrows() {
        when(currencyService.fetchDailyRate(any())).thenThrow(new RuntimeException("Boom"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> currencyController.getLatest(CurrencyCode.USD));

        assertEquals("Boom", ex.getMessage());
    }

    @Test
    void getHistory_success() {
        List<CurrencyRateDTO> history = List.of(
                new CurrencyRateDTO("EUR", BigDecimal.valueOf(4.5), LocalDateTime.parse("2025-01-01T00:00:00")),
                new CurrencyRateDTO("EUR", BigDecimal.valueOf(4.6), LocalDateTime.parse("2025-01-02T00:00:00"))
        );

        LocalDate from = LocalDate.parse("2025-01-01");
        LocalDate to = LocalDate.parse("2025-01-02");

        when(currencyService.fetchHistory(CurrencyCode.EUR, from, to)).thenReturn(history);

        List<CurrencyRateDTO> result = currencyController.getHistory(CurrencyCode.EUR, "2025-01-01", "2025-01-02");

        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(4.5), result.get(0).getRate());
        assertEquals(BigDecimal.valueOf(4.6), result.get(1).getRate());
        verify(currencyService, times(1)).fetchHistory(CurrencyCode.EUR, from, to);
    }

    @Test
    void getHistory_serviceThrows() {
        when(currencyService.fetchHistory(any(), any(), any())).thenThrow(new RuntimeException("Boom"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> currencyController.getHistory(CurrencyCode.EUR, "2025-01-01", "2025-01-02"));

        assertEquals("Boom", ex.getMessage());
    }

    @Test
    void getHistory_invalidDateFormat_throwsException() {
        assertThrows(Exception.class,
                () -> currencyController.getHistory(CurrencyCode.EUR, "01-01-2025", "2025-01-02"));
    }

    @Test
    void getAverage_success() {
        when(currencyService.getAverageRate(7, CurrencyCode.GBP)).thenReturn(5.123);

        Map<String, Object> result = currencyController.getAverage(CurrencyCode.GBP, 7);

        assertEquals(CurrencyCode.GBP, result.get("currency"));
        assertEquals(5.123, result.get("avg"));
        verify(currencyService, times(1)).getAverageRate(7, CurrencyCode.GBP);
    }

    @Test
    void getAverage_serviceThrows() {
        when(currencyService.getAverageRate(anyInt(), any())).thenThrow(new RuntimeException("Boom"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> currencyController.getAverage(CurrencyCode.GBP, 7));

        assertEquals("Boom", ex.getMessage());
    }

    @Test
    void getAverage_nullResult() {
        when(currencyService.getAverageRate(anyInt(), any())).thenReturn(0.0);

        Map<String, Object> result = currencyController.getAverage(CurrencyCode.USD, 5);

        assertEquals(0.0, result.get("avg"));
        assertEquals(CurrencyCode.USD, result.get("currency"));
    }

    @Test
    void getTrend_success() {
        when(currencyService.getTrend(30, CurrencyCode.USD)).thenReturn("UP");

        Map<String, Object> result = currencyController.getTrend(CurrencyCode.USD, 30);

        assertEquals(CurrencyCode.USD, result.get("currency"));
        assertEquals("UP", result.get("trend"));
        verify(currencyService, times(1)).getTrend(30, CurrencyCode.USD);
    }

    @Test
    void getTrend_serviceThrows() {
        when(currencyService.getTrend(anyInt(), any())).thenThrow(new RuntimeException("Boom"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> currencyController.getTrend(CurrencyCode.USD, 30));

        assertEquals("Boom", ex.getMessage());
    }

    @Test
    void getTrend_nullResult() {
        when(currencyService.getTrend(anyInt(), any())).thenReturn("stable");

        Map<String, Object> result = currencyController.getTrend(CurrencyCode.USD, 5);

        assertEquals("stable", result.get("trend")); // branch for rates.size() < 2
        assertEquals(CurrencyCode.USD, result.get("currency"));

        verify(currencyService, times(1)).getTrend(5, CurrencyCode.USD);
    }
}