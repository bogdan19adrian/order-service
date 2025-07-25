package com.bogdanenache.order_service.rest;

import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid OrderDTO orderDTO) {

        return ResponseEntity.ok(orderService.placeOrder(orderDTO));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable String internalId) {

      var orderDTO =  orderService.getOrderByInternalId(internalId);
        return orderDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @GetMapping("/orders?accountId={accountId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByAccountId(@RequestParam String accountId) {
        return ResponseEntity.ok(orderService.getOrderByAccountId(accountId));

    }

}
