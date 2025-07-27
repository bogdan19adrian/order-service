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
        return new WireMockContainer("wiremock/wiremock:3.6.0");
//                .withCopyToContainer(Transferable.of("/resource/wiremock"), "/home/wiremock/mappings");
//                .withCopyToContainer(Transferable.of("/resource/wiremock/2.json"), "/home/wiremock/mappings")
//                .withCopyToContainer(Transferable.of("/resource/wiremock/3.json"), "/home/wiremock/mappings")
//                .withCopyToContainer(Transferable.of("/resource/wiremock/4.json"), "/home/wiremock/mappings")
//                .withCopyToContainer(Transferable.of("/resource/wiremock/5.json"), "/home/wiremock/mappings")
//                .withCopyToContainer(Transferable.of("/resource/wiremock/badrequest.json"), "/home/wiremock/mappings")
//                .withCopyToContainer(Transferable.of("/resource/wiremock/error.json"), "/home/wiremock/mappings")
//                .withCopyToContainer(Transferable.of("/resource/wiremock/notfound.json"), "/home/wiremock/mappings");
    }

    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(WireMockContainer wiremockServer) {
        return registry -> {
            registry.add("order-service.price-feed-url", () -> wiremockServer.getBaseUrl() + "/price");
        };
    }

}
