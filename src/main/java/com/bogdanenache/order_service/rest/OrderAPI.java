package com.bogdanenache.order_service.rest;

import com.bogdanenache.order_service.dto.OrderDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface OrderAPI {

    @Operation(summary = "Create a new order", description = "Places a BUY or SELL order and executes it at the current price.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order successfully created",
                    content = @Content(schema = @Schema(implementation = OrderDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PostMapping("/orders")
    ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid OrderDTO orderDTO);


    @Operation(summary = "Get an order by ID", description = "Returns a single order if it exists.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @GetMapping("/orders/{id}")
    ResponseEntity<OrderDTO> getOrderById(
            @Parameter(description = "Internal order ID") @PathVariable("id") String id);


    @Operation(summary = "Get all orders for an account", description = "Returns a list of orders for the specified account ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found",
                    content = @Content(schema = @Schema(implementation = OrderDTO.class)))
    })
    @GetMapping("/orders")
    ResponseEntity<List<OrderDTO>> getOrdersByAccountId(
            @Parameter(description = "Account ID to fetch orders for") @RequestParam("accountId") String accountId);

}
