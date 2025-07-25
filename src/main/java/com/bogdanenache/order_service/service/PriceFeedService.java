package com.bogdanenache.order_service.service;
import com.bogdanenache.order_service.dto.PriceFeedResponse;
import com.bogdanenache.order_service.dto.PriceItem;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
            final ResponseEntity<PriceFeedResponse> response = restTemplate.getForEntity(priceFeedUrl, PriceFeedResponse.class);
            final PriceFeedResponse priceFeedResponse =  validateResponse(response);
            return priceFeedResponse.prices().stream()
                    .filter(priceItem -> priceItem.symbol().equals(symbol))
                    .map(PriceItem::price)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Price not found for symbol: " + symbol));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price for symbol: " + symbol, e);
        }
    }

    private PriceFeedResponse validateResponse(ResponseEntity<PriceFeedResponse> response) {
        if (response == null || response.getBody() == null || response.getBody().prices() == null) {
            throw new RuntimeException("Invalid price feed response: missing data");
        } else if (response.getBody().prices().isEmpty()) {
            throw new RuntimeException("Invalid price feed response: no prices available");
        }
        return response.getBody();
    }
}
