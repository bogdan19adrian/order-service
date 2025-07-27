package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.exception.UnexpectedException;
import java.math.BigDecimal;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.util.AopTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class PriceFeedRetryTest {

    @Autowired
    private PriceFeedService priceFeedService;

    @Test
    void shouldRetryOnceThenSucceed() {
        when(priceFeedService.getPrice(anyString()))
                .thenThrow(new UnexpectedException("Temporary failure"))
                .thenReturn(Optional.of(new BigDecimal("123.45")));

        // test that the PriceFeedService is being proxied for Retry
        Assertions.assertThat(AopUtils.isAopProxy(priceFeedService)).isTrue();
        // check that it's proxied by CGLIB since it's not implementing an interface
        Assertions.assertThat(AopUtils.isCglibProxy(priceFeedService)).isTrue();
        // get the mocked PriceFeedService so we can test expectations on it;
        PriceFeedService targetObject = AopTestUtils.getUltimateTargetObject(priceFeedService);

        priceFeedService.getPrice("AAPL");
        // 3 is the default max attempts set
        verify(targetObject, times(2)).getPrice(anyString());
    }

    @Test
    void shouldFailAfterAllRetries() {
        when(priceFeedService.getPrice(anyString()))
                .thenThrow(new UnexpectedException("Temporary failure"))
                .thenThrow(new UnexpectedException("Temporary failure"))
                .thenThrow(new UnexpectedException("Temporary failure"));

        // test that the PriceFeedService is being proxied for Retry
        Assertions.assertThat(AopUtils.isAopProxy(priceFeedService)).isTrue();
        // check that it's proxied by CGLIB since it's not implementing an interface
        Assertions.assertThat(AopUtils.isCglibProxy(priceFeedService)).isTrue();
        // get the mocked PriceFeedService so we can test expectations on it;
        PriceFeedService targetObject = AopTestUtils.getUltimateTargetObject(priceFeedService);

        priceFeedService.getPrice("AAPL");
        // 3 is the default max attempts set
        verify(targetObject, times(2)).getPrice(anyString());
    }

    @Configuration
    @EnableRetry(proxyTargetClass=true)
    static class SpringConfiguration {

        @Bean
        PriceFeedService priceFeedService() {
            return mock(PriceFeedService.class);
        }
    }

}


