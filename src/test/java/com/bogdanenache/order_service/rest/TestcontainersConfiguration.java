package com.bogdanenache.order_service.rest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"));
    }

    @Bean
    WireMockContainer wiremockServer() {
        return new WireMockContainer("wiremock/wiremock:3.6.0")
                .withMappingFromJSON("""
                        {
                          "request": {
                            "method": "GET",
                            "url": "/prices"
                          },
                          "response": {
                            "status": 200,
                            "headers": {
                              "Content-Type": "application/json"
                            },
                            "jsonBody": [
                              { "symbol": "AAPL", "price": 203.55 },
                              { "symbol": "GOOG", "price": 2731.45},
                              { "symbol": "MSFT", "price": 330.10 },
                              { "symbol": "AMZN", "price": 135.22 },
                              { "symbol": "TSLA", "price": 742.11 },
                              { "symbol": "META", "price": 245.10 },
                              { "symbol": "NVDA", "price": 780.23 },
                              { "symbol": "NFLX", "price": 415.93 },
                              { "symbol": "INTC", "price": 35.65 },
                              { "symbol": "ORCL", "price": 119.46 },
                              { "symbol": "IBM", "price": 145.77 },
                              { "symbol": "SAP", "price": 172.24 },
                              { "symbol": "AMD", "price": 113.83 },
                              { "symbol": "BABA", "price": 89.53 },
                              { "symbol": "UBER", "price": 45.33 },
                              { "symbol": "LYFT", "price": 11.74 },
                              { "symbol": "SHOP", "price": 62.45 },
                              { "symbol": "SQ", "price": 66.66 },
                              { "symbol": "PYPL", "price": 73.27 },
                              { "symbol": "SONY", "price": 91.12 }
                            ]
                          }
                        }
                        
                        """);
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(WireMockContainer wiremockServer) {
        return registry -> {
            registry.add("order-service.price-feed-url", () -> wiremockServer.getBaseUrl() + "/prices");
        };
    }

}
