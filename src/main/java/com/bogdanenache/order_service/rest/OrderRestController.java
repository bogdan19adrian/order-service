package com.bogdanenache.order_service.rest;

import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.service.OrderService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderRestController implements OrderAPI {

    private final OrderService orderService;

    @RateLimiter(name ="orderServiceRateLimiter")
    @PostMapping(value = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid OrderDTO orderDTO) {
        log.info("Received request to create a new order: {}", orderDTO);
        return new ResponseEntity<>(orderService.placeOrder(orderDTO), HttpStatus.CREATED);
    }

    @GetMapping(value = "/orders/{id}", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable(value = "id") String id) {
        log.info("Received request to get an order by id: {}", id);
        var orderDTO = orderService.getOrderByInternalId(id);
        return orderDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @GetMapping(value = "/orders", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderDTO>> getOrdersByAccountId(@RequestParam("accountId") String accountId) {
        log.info("Received request to get all orders by accountId: {}", accountId);
        return ResponseEntity.ok(orderService.getOrderByAccountId(accountId));

    }

}
