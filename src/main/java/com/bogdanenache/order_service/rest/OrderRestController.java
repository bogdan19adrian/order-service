package com.bogdanenache.order_service.rest;

import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.service.IdempotencyService;
import com.bogdanenache.order_service.service.OrderService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderRestController implements OrderAPI {

    private final OrderService orderService;
    private final IdempotencyService idempotencyService;

    @RateLimiter(name ="orderServiceRateLimiter")
    @PostMapping(value = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDTO> createOrder(
            @RequestBody @Valid OrderDTO orderDTO,
            @RequestHeader(value = "X-Idempotency-Key", required = true) @Min(20) @Max(36) String idempotencyKey) {
        log.info("Received request to create a new order: {}", orderDTO);

        // Validate idempotency key before placing the order
        idempotencyService.validateIdempotencyKey(idempotencyKey);

        return new ResponseEntity<>(orderService.placeOrder(orderDTO, idempotencyKey), HttpStatus.CREATED);
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
