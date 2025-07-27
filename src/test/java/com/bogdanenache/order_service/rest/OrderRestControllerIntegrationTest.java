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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = OrderServiceApplication.class)
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Testcontainers
public class OrderRestControllerIntegrationTest extends BaseTest {

    private static final String IDEMPOTENCY_HEADER_NAME = "X-Idempotency-Key";

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private Integer port;


    @Test
    @DisplayName("Calls API to create an order successfully")
    public void shouldCreateOrderSuccessfully() {
        var orderDTO = createOrder(10, "AAPL");
        HttpHeaders headers = new HttpHeaders();
        headers.set(IDEMPOTENCY_HEADER_NAME, UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(orderDTO, headers);

        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", httpEntity, OrderDTO.class);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(201, response.getStatusCode().value());
        Assertions.assertEquals(10, response.getBody().quantity());
        Assertions.assertEquals(Side.SELL.name(), response.getBody().side());
    }

    @Test
    @DisplayName("Calls API  to create an order with -1 quantity and fails")
    public void shouldFailCreateOrderIfQuantityIsNegative() {
        var orderDTO = createOrder(-1, "AAPL");
        HttpHeaders headers = new HttpHeaders();
        headers.set(IDEMPOTENCY_HEADER_NAME, UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(orderDTO, headers);

        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", httpEntity, ErrorResponse.class);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(400, response.getStatusCode().value());
        Assertions.assertEquals("Invalid value for field 'quantity': Quantity must be at least 1", response.getBody().message());
    }


    @Test
    @DisplayName("Calls API to get an order by id successfully")
    public void shouldGetOrderByIdSuccessfully() {
        var orderDTO = createOrder(5, "AAPL");
        HttpHeaders headers = new HttpHeaders();
        headers.set(IDEMPOTENCY_HEADER_NAME, UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(orderDTO, headers);

        var createResponse = restTemplate.postForEntity("http://localhost:" + port + "/orders", httpEntity, OrderDTO.class);
        String orderId = createResponse.getBody().id();

        var getResponse = restTemplate.getForEntity("http://localhost:" + port + "/orders/" + orderId, OrderDTO.class);
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertEquals(200, getResponse.getStatusCode().value());
        Assertions.assertEquals(orderId, getResponse.getBody().id());
        Assertions.assertNotNull(getResponse.getBody().execution());
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
        HttpHeaders headers = new HttpHeaders();
        headers.set(IDEMPOTENCY_HEADER_NAME, UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(orderDTO, headers);

        restTemplate.postForEntity("http://localhost:" + port + "/orders", httpEntity, OrderDTO.class);

        var getResponse = restTemplate.getForEntity("http://localhost:" + port + "/orders?accountId=" + orderDTO.accountId(), OrderDTO[].class);
        Assertions.assertEquals(200, getResponse.getStatusCode().value());
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertTrue(getResponse.getBody().length >= 1);
        Assertions.assertEquals(orderDTO.accountId(), getResponse.getBody()[0].accountId());
        Assertions.assertNotNull(getResponse.getBody()[0].execution());
    }
    @Test
    @DisplayName("Returns SERVICE_UNAVAILABLE when dependent service fails")
    public void shouldReturnServiceUnavailableWhenPriceFeedFails() {
        // Simulate a symbol that triggers UnexpectedException in your service
        var orderDTO = createOrder(1, "error");
        HttpHeaders headers = new HttpHeaders();
        headers.set(IDEMPOTENCY_HEADER_NAME, UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(orderDTO, headers);

        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", httpEntity, ErrorResponse.class);
        Assertions.assertEquals(503, response.getStatusCode().value());
        Assertions.assertEquals("Service is currently unavailable", response.getBody().message());
    }

    @Test
    @DisplayName("Returns UNPROCESSABLE_ENTITY for business validation error")
    public void shouldReturnUnprocessableEntityForBusinessError() {
        // Simulate a request that triggers BadRequestException (e.g., invalid symbol)
        var orderDTO = createOrder(1, "badrequest");
        HttpHeaders headers = new HttpHeaders();
        headers.set(IDEMPOTENCY_HEADER_NAME, UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(orderDTO, headers);

        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", httpEntity, ErrorResponse.class);
        Assertions.assertEquals(422, response.getStatusCode().value());
        Assertions.assertEquals("An unexpected error occurred while processing the request", response.getBody().message());
    }

    @Test
    @DisplayName("Calls API and fails if idempotency header is missing")
    public void shouldFailToCreateOrderIfIdempotencyHeaderIsMissing() {
        var orderDTO = createOrder(10, "AAPL");

        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", orderDTO, ErrorResponse.class);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(400, response.getStatusCode().value());
        Assertions.assertEquals("Idempotency key null is invalid.", response.getBody().message());

    }
    @Test
    @DisplayName("Calls API and fails if idempotency header is larger than 36")
    public void shouldFailToCreateOrderIfIdempotencyHeaderIsLargerThan36() {
        var orderDTO = createOrder(10, "AAPL");
        HttpHeaders headers = new HttpHeaders();
        var headerValue = UUID.randomUUID().toString() + "aaaaa";
        headers.set(IDEMPOTENCY_HEADER_NAME, headerValue);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(orderDTO, headers);
        var response = restTemplate.postForEntity("http://localhost:" + port + "/orders", httpEntity, ErrorResponse.class);

        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(400, response.getStatusCode().value());
        Assertions.assertEquals("Idempotency key " + headerValue +" is invalid.", response.getBody().message());

    }

    @Test
    @DisplayName("Calls API and fails if idempotency header is reused")
    public void shouldFailToCreateOrderIfIdempotencyHeaderIsReused() {
        var orderDTO = createOrder(10, "AAPL");
        HttpHeaders headers = new HttpHeaders();
        String idempotencyKeyHeader = UUID.randomUUID().toString();
        headers.set(IDEMPOTENCY_HEADER_NAME, idempotencyKeyHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderDTO> httpEntity = new HttpEntity<>(orderDTO, headers);

        var responseSuccessfully = restTemplate.postForEntity("http://localhost:" + port + "/orders", httpEntity, OrderDTO.class);
        var responseUnuccessfully = restTemplate.postForEntity("http://localhost:" + port + "/orders", httpEntity, ErrorResponse.class);

        Assertions.assertNotNull(responseSuccessfully.getBody());
        Assertions.assertEquals(201, responseSuccessfully.getStatusCode().value());

        Assertions.assertNotNull(responseUnuccessfully.getBody());
        Assertions.assertEquals(400, responseUnuccessfully.getStatusCode().value());
        Assertions.assertEquals("Idempotency key " + idempotencyKeyHeader +" is already used.", responseUnuccessfully.getBody().message());

    }

}
