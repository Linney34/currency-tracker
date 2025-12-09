package com.example.currencytracker.serviceTest;

import com.example.currencytracker.dto.CurrencyRateDTO;
import com.example.currencytracker.enums.CurrencyCode;
import com.example.currencytracker.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyServiceTest {

    private RestTemplate restTemplate;
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        currencyService = new CurrencyService(restTemplate);
    }

    @Test
    void fetchDailyRate_success() {
        Map<String, Object> response = new HashMap<>();
        response.put("rates", Map.of("PLN", 4.5));
        response.put("date", "2025-12-09");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        CurrencyRateDTO dto = currencyService.fetchDailyRate(CurrencyCode.USD);

        assertEquals("USD", dto.getCurrency());
        assertEquals(BigDecimal.valueOf(4.5), dto.getRate());
        assertEquals(LocalDate.parse("2025-12-09").atTime(16,0), dto.getTimestamp());
    }

    @Test
    void fetchDailyRate_noRates_throwsException() {
        Map<String, Object> response = new HashMap<>();
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                currencyService.fetchDailyRate(CurrencyCode.EUR));
        assertTrue(ex.getMessage().contains("No rates found"));
    }

    @Test
    void fetchDailyRate_restClientException_throwsException() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("API down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                currencyService.fetchDailyRate(CurrencyCode.GBP));
        assertTrue(ex.getMessage().contains("Failed to fetch latest rate"));
    }

    @Test
    void fetchHistory_success() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Map<String, Double>> ratesMap = new HashMap<>();
        ratesMap.put("2025-12-01", Map.of("PLN", 4.5));
        ratesMap.put("2025-12-02", Map.of("PLN", 4.6));
        response.put("rates", ratesMap);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        List<CurrencyRateDTO> history = currencyService.fetchHistory(CurrencyCode.USD,
                LocalDate.parse("2025-12-01"), LocalDate.parse("2025-12-02"));

        assertEquals(2, history.size());
        assertEquals(BigDecimal.valueOf(4.5), history.get(0).getRate());
        assertEquals(BigDecimal.valueOf(4.6), history.get(1).getRate());
    }

    @Test
    void fetchHistory_noRates_throwsException() {
        Map<String, Object> response = new HashMap<>();
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                currencyService.fetchHistory(CurrencyCode.CHF,
                        LocalDate.now().minusDays(2), LocalDate.now()));
        assertTrue(ex.getMessage().contains("No historical rates found"));
    }

    @Test
    void fetchHistory_restClientException_throwsException() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("API down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                currencyService.fetchHistory(CurrencyCode.JPY,
                        LocalDate.now().minusDays(2), LocalDate.now()));
        assertTrue(ex.getMessage().contains("Failed to fetch history"));
    }

    @Test
    void getAverageRate_calculatesCorrectly() {
        CurrencyRateDTO rate1 = CurrencyRateDTO.builder()
                .currency("USD").rate(BigDecimal.valueOf(4.0)).timestamp(LocalDate.now().atTime(16,0)).build();
        CurrencyRateDTO rate2 = CurrencyRateDTO.builder()
                .currency("USD").rate(BigDecimal.valueOf(6.0)).timestamp(LocalDate.now().atTime(16,0)).build();

        CurrencyService spyService = spy(currencyService);
        doReturn(Arrays.asList(rate1, rate2)).when(spyService).fetchHistory(any(), any(), any());

        double avg = spyService.getAverageRate(2, CurrencyCode.USD);
        assertEquals(5.0, avg);
    }

    @Test
    void getAverageRate_emptyHistory_returnsZero() {
        CurrencyService spyService = spy(currencyService);
        doReturn(Collections.emptyList()).when(spyService).fetchHistory(any(), any(), any());

        double avg = spyService.getAverageRate(3, CurrencyCode.EUR);
        assertEquals(0.0, avg);
    }

    @Test
    void getTrend_up() {
        CurrencyRateDTO rate1 = CurrencyRateDTO.builder()
                .currency("USD").rate(BigDecimal.valueOf(4.0)).timestamp(LocalDate.now().atTime(16,0)).build();
        CurrencyRateDTO rate2 = CurrencyRateDTO.builder()
                .currency("USD").rate(BigDecimal.valueOf(5.0)).timestamp(LocalDate.now().atTime(16,0)).build();

        CurrencyService spyService = spy(currencyService);
        doReturn(Arrays.asList(rate1, rate2)).when(spyService).fetchHistory(any(), any(), any());

        String trend = spyService.getTrend(2, CurrencyCode.USD);
        assertEquals("up", trend);
    }

    @Test
    void getTrend_down() {
        CurrencyRateDTO rate1 = CurrencyRateDTO.builder()
                .currency("USD").rate(BigDecimal.valueOf(5.0)).timestamp(LocalDate.now().atTime(16,0)).build();
        CurrencyRateDTO rate2 = CurrencyRateDTO.builder()
                .currency("USD").rate(BigDecimal.valueOf(4.0)).timestamp(LocalDate.now().atTime(16,0)).build();

        CurrencyService spyService = spy(currencyService);
        doReturn(Arrays.asList(rate1, rate2)).when(spyService).fetchHistory(any(), any(), any());

        String trend = spyService.getTrend(2, CurrencyCode.USD);
        assertEquals("down", trend);
    }

    @Test
    void getTrend_stable() {
        CurrencyRateDTO rate1 = CurrencyRateDTO.builder()
                .currency("USD").rate(BigDecimal.valueOf(4.0)).timestamp(LocalDate.now().atTime(16,0)).build();
        CurrencyRateDTO rate2 = CurrencyRateDTO.builder()
                .currency("USD").rate(BigDecimal.valueOf(4.0)).timestamp(LocalDate.now().atTime(16,0)).build();

        CurrencyService spyService = spy(currencyService);
        doReturn(Arrays.asList(rate1, rate2)).when(spyService).fetchHistory(any(), any(), any());

        String trend = spyService.getTrend(2, CurrencyCode.USD);
        assertEquals("stable", trend);
    }

    @Test
    void getTrend_notEnoughData_returnsStable() {
        CurrencyRateDTO rate1 = CurrencyRateDTO.builder()
                .currency("USD").rate(BigDecimal.valueOf(4.0)).timestamp(LocalDate.now().atTime(16,0)).build();

        CurrencyService spyService = spy(currencyService);
        doReturn(Collections.singletonList(rate1)).when(spyService).fetchHistory(any(), any(), any());

        String trend = spyService.getTrend(1, CurrencyCode.USD);
        assertEquals("stable", trend);
    }

    @Test
    void getTrend_fetchHistoryThrows_throwsException() {
        CurrencyService spyService = spy(currencyService);
        doThrow(new RuntimeException("API down")).when(spyService).fetchHistory(any(), any(), any());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                spyService.getTrend(3, CurrencyCode.USD));
        assertTrue(ex.getMessage().contains("Failed to fetch history"));
    }
}