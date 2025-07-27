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


    @Test
    @DisplayName("Calls API to get an order by id successfully")
    public void shouldGetOrderByIdSuccessfully() {
        var orderDTO = createOrder(5, "AAPL");
        var createResponse = restTemplate.postForEntity("http://localhost:" + port + "/orders", orderDTO, OrderDTO.class);
        String orderId = createResponse.getBody().id();

        var getResponse = restTemplate.getForEntity("http://localhost:" + port + "/orders/" + orderId, OrderDTO.class);
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertEquals(200, getResponse.getStatusCode().value());
        Assertions.assertEquals(orderId, getResponse.getBody().id());
    }

    @Test
    @DisplayName("Calls API to get an order by id and returns not found")
    public void shouldReturnNotFoundForInvalidOrderId() {
        var getResponse = restTemplate.getForEntity("http://localhost:" + port + "/orders/invalid-id", OrderDTO.class);
        Assertions.assertEquals(404, getResponse.getStatusCode().value());
    }

    @Test
    @DisplayName("Calls API to get orders by accountId successfully")
    public void shouldGetOrdersByAccountIdSuccessfully() {
        var orderDTO = createOrder(3, "AAPL");
        restTemplate.postForEntity("http://localhost:" + port + "/orders", orderDTO, OrderDTO.class);

        var getResponse = restTemplate.getForEntity("http://localhost:" + port + "/orders?accountId=" + orderDTO.accountId(), OrderDTO[].class);
        Assertions.assertEquals(200, getResponse.getStatusCode().value());
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertTrue(getResponse.getBody().length >= 1);
        Assertions.assertEquals(orderDTO.accountId(), getResponse.getBody()[0].accountId());
    }
    @Test
    @DisplayName("Returns SERVICE_UNAVAILABLE when dependent service fails")
    public void shouldReturnServiceUnavailableWhenPriceFeedFails() {
        // Simulate a symbol that triggers UnexpectedException in your service
        var orderDTO = createOrder(1, "error");
        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", orderDTO, ErrorResponse.class);
        Assertions.assertEquals(503, response.getStatusCode().value());
        Assertions.assertEquals("Service is currently unavailable", response.getBody().message());
    }

    @Test
    @DisplayName("Returns UNPROCESSABLE_ENTITY for business validation error")
    public void shouldReturnUnprocessableEntityForBusinessError() {
        // Simulate a request that triggers BadRequestException (e.g., invalid symbol)
        var orderDTO = createOrder(1, "badrequest");
        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", orderDTO, ErrorResponse.class);
        Assertions.assertEquals(422, response.getStatusCode().value());
        Assertions.assertEquals("An unexpected error occurred while processing the request", response.getBody().message());
    }
}
