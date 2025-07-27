package com.bogdanenache.order_service.rest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.Transferable;
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
                .withMappingFromResource("wiremock/a1.json")
                .withMappingFromResource("wiremock/a2.json")
                .withMappingFromResource("wiremock/a3.json")
                .withMappingFromResource("wiremock/a4.json")
                .withMappingFromResource("wiremock/a5.json")
                .withMappingFromResource("wiremock/error.json")
                .withMappingFromResource("wiremock/badrequest.json")
                .withMappingFromResource("wiremock/notfound.json")
                .withMappingFromResource("wiremock/smallprice.json")
                .withMappingFromResource("wiremock/bigprice.json");
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(WireMockContainer wiremockServer) {
        return registry -> {
            registry.add("order-service.price-feed-url", () -> wiremockServer.getBaseUrl() + "/price");
        };
    }

}
