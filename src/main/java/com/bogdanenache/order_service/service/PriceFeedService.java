package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.dto.PriceItem;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PriceFeedService {

    private final RestTemplate restTemplate;

    @Value("${order-service.price-feed-url}")
    private String priceFeedUrl; // WireMock URL


    public BigDecimal getPrice(String symbol) {
        try {
            final ResponseEntity<List<PriceItem>> response = restTemplate.exchange(priceFeedUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<PriceItem>>() {
                    });
            final List<PriceItem> priceFeedResponse = validateResponse(response);
            return priceFeedResponse.stream()
                    .filter(priceItem -> priceItem.symbol().equals(symbol))
                    .map(PriceItem::price)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Price not found for symbol: " + symbol));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price for symbol: " + symbol, e);
        }
    }

    private List<PriceItem> validateResponse(ResponseEntity<List<PriceItem>> response) {
        if (response == null || response.getBody() == null || response.getBody().isEmpty()) {
            throw new RuntimeException("Invalid price feed response: missing data");
        }
        return response.getBody();
    }
}
