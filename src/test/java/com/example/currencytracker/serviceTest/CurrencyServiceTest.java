package com.example.currencytracker.serviceTest;

import com.example.currencytracker.entity.CurrencyRate;
import com.example.currencytracker.repository.CurrencyRateRepository;
import com.example.currencytracker.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyServiceTest {

    @Mock
    private CurrencyRateRepository repository;

    @InjectMocks
    private CurrencyService currencyService;

    @Spy
    private RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveRate_savesRate() {
        CurrencyRate rate = CurrencyRate.builder().currency("USD").rate(BigDecimal.ONE).timestamp(LocalDateTime.now()).build();
        currencyService.saveRate(rate);
        verify(repository).save(rate);
    }

    @Test
    void fetchLatestRates_successfulFetch() {
        Map<String, Object> rateMap = Map.of("mid", 4.5);
        Map<String, Object> response = Map.of("rates", List.of(rateMap));

        doReturn(response).when(restTemplate).getForObject(anyString(), eq(Map.class));

        currencyService.fetchLatestRates();

        verify(repository, times(2)).save(any(CurrencyRate.class));
    }

    @Test
    void fetchLatestRates_nullResponse() {
        doReturn(null).when(restTemplate).getForObject(anyString(), eq(Map.class));
        currencyService.fetchLatestRates(); // should not throw
        verify(repository, never()).save(any());
    }

    @Test
    void fetchLatestRates_emptyRatesList() {
        Map<String, Object> response = Map.of("rates", List.of());
        doReturn(response).when(restTemplate).getForObject(anyString(), eq(Map.class));
        currencyService.fetchLatestRates();
        verify(repository, never()).save(any());
    }

    @Test
    void fetchLatestRates_exceptionThrown() {
        doThrow(new RestClientException("boom")).when(restTemplate).getForObject(anyString(), eq(Map.class));
        currencyService.fetchLatestRates(); // should catch exception
        verify(repository, never()).save(any());
    }

    @Test
    void getLatestRate_returnsLatestRate() {
        CurrencyRate rate = CurrencyRate.builder().currency("USD").rate(BigDecimal.TEN).timestamp(LocalDateTime.now()).build();
        when(repository.findTopByCurrencyOrderByTimestampDesc("USD")).thenReturn(rate);

        CurrencyRate result = currencyService.getLatestRate("USD");
        assertEquals(rate, result);
    }

    @Test
    void getHistory_returnsRatesBetweenDates() {
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now();
        List<CurrencyRate> rates = List.of(
                CurrencyRate.builder().currency("USD").rate(BigDecimal.ONE).timestamp(LocalDateTime.now()).build()
        );
        when(repository.findByCurrencyAndTimestampBetween(eq("USD"), any(), any())).thenReturn(rates);

        List<CurrencyRate> result = currencyService.getHistory("USD", from, to);
        assertEquals(rates, result);
    }

    @Test
    void getAverage_returnsZeroForEmptyRates() {
        when(repository.findByCurrencyAndTimestampBetween(anyString(), any(), any())).thenReturn(Collections.emptyList());
        BigDecimal avg = currencyService.getAverage("USD", 5);
        assertEquals(BigDecimal.ZERO, avg);
    }

    @Test
    void getAverage_returnsCorrectAverage() {
        List<CurrencyRate> rates = List.of(
                CurrencyRate.builder().currency("USD").rate(BigDecimal.valueOf(2)).build(),
                CurrencyRate.builder().currency("USD").rate(BigDecimal.valueOf(4)).build()
        );
        when(repository.findByCurrencyAndTimestampBetween(anyString(), any(), any())).thenReturn(rates);

        BigDecimal avg = currencyService.getAverage("USD", 2);
        assertEquals(BigDecimal.valueOf(3.0000).setScale(4), avg);
    }

    @Test
    void getTrend_returnsStableForLessThanTwoRates() {
        List<CurrencyRate> rates = List.of(
                CurrencyRate.builder().currency("USD").rate(BigDecimal.ONE).build()
        );
        when(repository.findByCurrencyAndTimestampBetween(anyString(), any(), any())).thenReturn(rates);

        String trend = currencyService.getTrend("USD");
        assertEquals("stable", trend);
    }

    @Test
    void getTrend_returnsUp() {
        List<CurrencyRate> rates = new LinkedList<>();
        rates.add(CurrencyRate.builder().currency("USD").rate(BigDecimal.ONE).build());
        rates.add(CurrencyRate.builder().currency("USD").rate(BigDecimal.TEN).build());

        when(repository.findByCurrencyAndTimestampBetween(anyString(), any(), any())).thenReturn(rates);

        String trend = currencyService.getTrend("USD");
        assertEquals("up", trend);
    }

    @Test
    void getTrend_returnsDown() {
        List<CurrencyRate> rates = new LinkedList<>();
        rates.add(CurrencyRate.builder().currency("USD").rate(BigDecimal.TEN).build());
        rates.add(CurrencyRate.builder().currency("USD").rate(BigDecimal.ONE).build());

        when(repository.findByCurrencyAndTimestampBetween(anyString(), any(), any())).thenReturn(rates);

        String trend = currencyService.getTrend("USD");
        assertEquals("down", trend);
    }

    @Test
    void getTrend_returnsStableWhenFirstEqualsLast() {
        List<CurrencyRate> rates = new LinkedList<>();
        rates.add(CurrencyRate.builder().currency("USD").rate(BigDecimal.TEN).build());
        rates.add(CurrencyRate.builder().currency("USD").rate(BigDecimal.TEN).build());

        when(repository.findByCurrencyAndTimestampBetween(anyString(), any(), any())).thenReturn(rates);

        String trend = currencyService.getTrend("USD");
        assertEquals("stable", trend);
    }
}