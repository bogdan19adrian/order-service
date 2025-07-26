package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.dto.PriceItem;
import com.bogdanenache.order_service.exception.BadRequestException;
import com.bogdanenache.order_service.exception.UnexpectedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PriceFeedServiceTest {

    private RestTemplate restTemplate;
    private PriceFeedService priceFeedService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        priceFeedService = new PriceFeedService(restTemplate);
        // Set private field priceFeedUrl via reflection for testing
        try {
            var field = PriceFeedService.class.getDeclaredField("priceFeedUrl");
            field.setAccessible(true);
            field.set(priceFeedService, "http://mock-url");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getPrice_shouldReturnPrice_whenSymbolExists() {
        PriceItem item = new PriceItem("AAPL", BigDecimal.TEN);
        List<PriceItem> items = List.of(item);
        ResponseEntity<List<PriceItem>> response = ResponseEntity.ok(items);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        Optional<BigDecimal> result = priceFeedService.getPrice("AAPL");
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.TEN, result.get());
    }

    @Test
    void getPrice_shouldThrowBadRequest_whenSymbolNotFound() {
        PriceItem item = new PriceItem("GOOG", BigDecimal.ONE);
        List<PriceItem> items = List.of(item);
        ResponseEntity<List<PriceItem>> response = ResponseEntity.ok(items);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        assertThrows(BadRequestException.class, () -> priceFeedService.getPrice("AAPL"));
    }

    @Test
    void getPrice_shouldThrowUnexpected_whenResponseIsNull() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(null);

        assertThrows(UnexpectedException.class, () -> priceFeedService.getPrice("AAPL"));
    }

    @Test
    void getPrice_shouldThrowUnexpected_whenRestTemplateThrows() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(UnexpectedException.class, () -> priceFeedService.getPrice("AAPL"));
    }
}