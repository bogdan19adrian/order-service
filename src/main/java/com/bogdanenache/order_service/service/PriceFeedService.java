package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.dto.PriceItem;
import com.bogdanenache.order_service.exception.BadRequestException;
import com.bogdanenache.order_service.exception.BadRequestException.Message;
import com.bogdanenache.order_service.exception.ErrorCode;
import com.bogdanenache.order_service.exception.UnexpectedException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static com.bogdanenache.order_service.exception.UnexpectedException.Message.FAILED_TO_FETCH_SYMBOL;
import static com.bogdanenache.order_service.exception.UnexpectedException.Message.INVALID_RESPONSE;

/**
 * Service class responsible for interacting with the price feed API.
 * Provides functionality to fetch prices for specific symbols with retry mechanisms.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceFeedService {

    private final RestTemplate restTemplate;

    @Value("${order-service.price-feed-url}")
    private String priceFeedUrl; // WireMock URL

    /**
     * Fetches the price for a given symbol from the price feed API.
     * Implements retry logic for handling transient errors.
     *
     * @param symbol the symbol for which the price is to be fetched
     * @return an Optional containing the price if available, or empty if not
     * @throws BadRequestException if the symbol is not found or the request is invalid
     * @throws UnexpectedException for server errors or unexpected runtime exceptions
     */
    @Retryable(retryFor = UnexpectedException.class,
            maxAttemptsExpression = "#{${order-service.retry.maxAttempts:3}}",
            backoff = @Backoff(delayExpression = "#{${order-service.retry.backoff.delay:200}}",
                    maxDelayExpression = "#{${order-service.retry.backoff.maxDelay:500}}"))
    public Optional<BigDecimal> getPrice(String symbol) {
        final ResponseEntity<PriceItem> response;
        try {
            String url = priceFeedUrl + "?symbol={symbol}";
            response = restTemplate.getForEntity(url, PriceItem.class, symbol);

        } catch (HttpServerErrorException e) {
            log.error("Server error while fetching price for symbol: {}", symbol, e);
            throw new UnexpectedException(FAILED_TO_FETCH_SYMBOL.with(symbol), ErrorCode.INTERNAL_SERVER_ERROR, e);
        } catch (HttpClientErrorException e) {
            log.error("Client error while fetching price for symbol: {}", symbol, e);
            throw new BadRequestException(Message.PRICE_NOT_FOUND_FOR_SYMBOL.with(symbol), ErrorCode.BAD_REQUEST, e);
        } catch (RuntimeException e) {
            log.error("Unexpected error while fetching price for symbol: {}", symbol, e);
            throw new UnexpectedException(FAILED_TO_FETCH_SYMBOL.with(symbol), ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
        final PriceItem priceFeedResponse = validateResponse(response);

        return Optional.of(priceFeedResponse.price());
    }

    /**
     * Validates the response from the price feed API.
     * Ensures the response and its body are not null.
     *
     * @param response the ResponseEntity containing the price feed data
     * @return the PriceItem object from the response body
     * @throws UnexpectedException if the response or its body is null
     */
    private PriceItem validateResponse(ResponseEntity<PriceItem> response) {
        if (response == null || response.getBody() == null) {
            throw new UnexpectedException(INVALID_RESPONSE.getFormatMessage(), ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return response.getBody();
    }

    /**
     * Recovery method for handling failures after all retry attempts are exhausted.
     * Logs the retry count and recovery time, then rethrows the exception.
     *
     * @param unexpectedException the exception that caused the failure
     * @param symbol the symbol for which the price was being fetched
     * @throws UnexpectedException rethrows the original exception after logging recovery details
     */
    @Recover
    public Optional<BigDecimal> recover(UnexpectedException unexpectedException, String symbol) {
        log.info("Retry Number: {} ", RetrySynchronizationManager.getContext().getRetryCount());
        log.info("Retry connection to price feed recovered at : {}", LocalDateTime.now());
        throw unexpectedException;
    }

}