package com.bogdanenache.order_service.rest;

import com.bogdanenache.order_service.BaseTest;
import com.bogdanenache.order_service.OrderServiceApplication;
import com.bogdanenache.order_service.dto.ErrorResponse;
import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.dto.Side;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = OrderServiceApplication.class)
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Testcontainers
public class OrderRestControllerIntegrationTest extends BaseTest {


    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private Integer port;


    @Test
    @DisplayName("Calls API to create an order successfully")
    public void shouldCreateOrderSuccessfully() {
        var orderDTO = createOrder(10, "AAPL");
        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", orderDTO, OrderDTO.class);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(201, response.getStatusCode().value());
        Assertions.assertEquals(10, response.getBody().quantity());
        Assertions.assertEquals(Side.SELL.name(), response.getBody().side());
    }

    @Test
    @DisplayName("Calls API  to create an order with -1 quantity and fails")
    public void shouldFailCreateOrderIfQuantityIsNegative() {
        var orderDTO = createOrder(-1, "AAPL");
        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", orderDTO, ErrorResponse.class);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(400, response.getStatusCode().value());
        Assertions.assertEquals("Invalid value for field 'quantity': Quantity must be at least 1", response.getBody().message());
    }

}
