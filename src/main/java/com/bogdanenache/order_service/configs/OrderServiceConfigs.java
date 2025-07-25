package com.bogdanenache.order_service.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OrderServiceConfigs {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
