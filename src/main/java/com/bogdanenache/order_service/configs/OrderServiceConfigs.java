package com.bogdanenache.order_service.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for defining beans and application-specific configurations.
 */
@Configuration
public class OrderServiceConfigs {

    /**
     * Creates and provides a RestTemplate bean for making REST API calls.
     *
     * @return a new instance of RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}