package com.bogdanenache.order_service.rest;

import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> createOrder(OrderDTO orderDTO) {
        // Here you would typically call a service to handle the business logic
        // For now, we just return the received orderDTO as a response
        return ResponseEntity.ok(orderDTO);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String internalId) {
        // Here you would typically call a service to handle the business logic
        // For now, we just return the received orderDTO as a response
        return ResponseEntity.ok(null);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<List<OrderDTO>> getOrders(@RequestParam String accountId) {
        // Here you would typically call a service to handle the business logic
        // For now, we just return the received orderDTO as a response
        return ResponseEntity.ok(null);
    }

}
