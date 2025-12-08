package com.example.currencytracker.controllerTest;

import com.example.currencytracker.controller.CurrencyController;
import com.example.currencytracker.dto.CurrencyRateDTO;
import com.example.currencytracker.entity.CurrencyRate;
import com.example.currencytracker.service.CurrencyService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyControllerTest {

    private final CurrencyService service = mock(CurrencyService.class);
    private final CurrencyController controller = new CurrencyController(service);

    @Test
    void testGetLatest() {
        CurrencyRate rate = new CurrencyRate(1L, "USD", BigDecimal.ONE, LocalDateTime.now());
        when(service.getLatestRate("USD")).thenReturn(rate);

        CurrencyRateDTO dto = controller.getLatest("USD");

        assertEquals("USD", dto.getCurrency());
        assertEquals(BigDecimal.ONE, dto.getRate());
    }

    @Test
    void testGetHistory() {
        List<CurrencyRate> mockList = List.of(
                new CurrencyRate(1L, "EUR", BigDecimal.TEN, LocalDateTime.now())
        );

        when(service.getHistory(any(), any(), any())).thenReturn(mockList);

        List<CurrencyRateDTO> result = controller.getHistory("EUR", LocalDate.now(), LocalDate.now());

        assertEquals(1, result.size());
        assertEquals("EUR", result.get(0).getCurrency());
    }

    @Test
    void testGetAverage() {
        when(service.getAverage("USD", 5)).thenReturn(BigDecimal.valueOf(4.5));

        assertEquals(BigDecimal.valueOf(4.5), controller.getAverage("USD", 5));
    }

    @Test
    void testGetTrend() {
        when(service.getTrend("USD")).thenReturn("up");

        assertEquals("up", controller.getTrend("USD"));
    }
}