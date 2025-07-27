package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.dto.PriceItem;
import com.bogdanenache.order_service.exception.UnexpectedException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PriceFeedServiceTest {

    private RestTemplate restTemplate;
    private PriceFeedService priceFeedService;
    private final String url = "http://mock-url";
    private final String path = "?symbol={symbol}";

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        priceFeedService = new PriceFeedService(restTemplate);
        // Set private field priceFeedUrl via reflection for testing
        try {
            var field = PriceFeedService.class.getDeclaredField("priceFeedUrl");
            field.setAccessible(true);
            field.set(priceFeedService, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getPrice_shouldReturnPrice_whenSymbolExists() {
        PriceItem item = new PriceItem("AAPL", BigDecimal.TEN);
        ResponseEntity<PriceItem> response = ResponseEntity.ok(item);

        when(restTemplate.getForEntity(url + path, PriceItem.class, "AAPL"))
                .thenReturn(response);

        Optional<BigDecimal> result = priceFeedService.getPrice("AAPL");
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.TEN, result.get());
    }

    @Test
    void getPrice_shouldThrowBadRequest_whenSymbolNotFound() {

        PriceItem item = new PriceItem("GOOG", BigDecimal.ONE);
        ResponseEntity<PriceItem> response = ResponseEntity.ok(item);

        when(restTemplate.getForEntity(url + path, PriceItem.class, "GOOG"))
                .thenReturn(response);
        assertThrows(UnexpectedException.class, () -> priceFeedService.getPrice("AAPL"));
    }

    @Test
    void getPrice_shouldThrowUnexpected_whenResponseIsNull() {
        when(restTemplate.getForEntity(url + path, PriceItem.class, "AAPL"))
                .thenReturn(null);

        assertThrows(UnexpectedException.class, () -> priceFeedService.getPrice("AAPL"));
    }

    @Test
    void getPrice_shouldThrowUnexpected_whenRestTemplateThrows() {
        when(restTemplate.getForEntity(url + path, PriceItem.class, "AAPL"))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(UnexpectedException.class, () -> priceFeedService.getPrice("AAPL"));
    }
}