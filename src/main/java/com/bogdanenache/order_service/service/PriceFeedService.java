package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.dto.PriceItem;
import com.bogdanenache.order_service.exception.BadRequestException;
import com.bogdanenache.order_service.exception.BadRequestException.Message;
import com.bogdanenache.order_service.exception.ErrorCode;
import com.bogdanenache.order_service.exception.UnexpectedException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.bogdanenache.order_service.exception.UnexpectedException.Message.FAILED_TO_FETCH_SYMBOL;
import static com.bogdanenache.order_service.exception.UnexpectedException.Message.INVALID_RESPONSE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceFeedService {

    private final RestTemplate restTemplate;

    @Value("${order-service.price-feed-url}")
    private String priceFeedUrl; // WireMock URL


    @Retryable(retryFor = { RuntimeException.class },
            maxAttemptsExpression = "#{${order-service.retry.maxAttempts:3}}",
            backoff = @Backoff(delayExpression = "#{${order-service.retry.backoff.delay:200}}",
                    maxDelayExpression = "#{${order-service.retry.backoff.maxDelay:500}}"))
    public Optional<BigDecimal> getPrice(String symbol) {
        final ResponseEntity<List<PriceItem>> response;
        try {
            response = restTemplate.exchange(priceFeedUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<PriceItem>>() {
                    });

        } catch (Exception e) {
            log.error("Failed to fetch price for symbol: {}", symbol, e);
            throw new UnexpectedException(FAILED_TO_FETCH_SYMBOL.with(symbol), ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
        final List<PriceItem> priceFeedResponse = validateResponse(response);
        var price = priceFeedResponse.stream()
                .filter(priceItem -> priceItem.symbol().equals(symbol))
                .map(PriceItem::price)
                .findFirst()
                .orElseThrow(() -> new BadRequestException(Message.PRICE_NOT_FOUND_FOR_SYMBOL.with(symbol), ErrorCode.BAD_REQUEST));
        return Optional.of(price);
    }

    private List<PriceItem> validateResponse(ResponseEntity<List<PriceItem>> response) {
        if (response == null || response.getBody() == null || response.getBody().isEmpty()) {
            throw new UnexpectedException(INVALID_RESPONSE.getFormatMessage(), ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return response.getBody();
    }

    @Recover
    public void recover(UnexpectedException ex) {
        log.info("Retry connection to price feed recovered at : {}", LocalDateTime.now());
        log.error(ex.getMessage(), ex);
        throw ex;
    }
}
