package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.dao.entity.Execution;
import com.bogdanenache.order_service.dao.entity.Order;
import com.bogdanenache.order_service.dao.entity.Order.OrderStatus;
import com.bogdanenache.order_service.dao.repository.OrderRepository;
import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.exception.BadRequestException;
import com.bogdanenache.order_service.exception.BadRequestException.Message;
import com.bogdanenache.order_service.exception.ErrorCode;
import com.bogdanenache.order_service.mapper.ExecutionMapper;
import com.bogdanenache.order_service.mapper.OrderMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling order-related operations.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final PriceFeedService priceFeed;
    private final OrderRepository orderRepo;

    /**
     * Populates an Execution entity with the given order and price.
     *
     * @param order the order associated with the execution
     * @param price the price at which the order is executed
     * @return a populated Execution entity
     */
    private static Execution populateExecution(Order order, BigDecimal price) {
        return Execution.builder()
                .order(order)
                .price(price)
                .internalId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .build();
    }

    /**
     * Places an order based on the provided OrderDTO.
     * Handles price retrieval and order processing logic.
     * If the price is unavailable, the order is marked as FAILED.
     *
     * @param orderDTO the data transfer object containing order details
     * @param idempotencyKey to be saved along the order to ensure idempotency
     * @return the processed OrderDTO with execution details if applicable
     * @throws BadRequestException if the price for the symbol is not found
     * @throws ExhaustedRetryException if other unexpected errors occur during price retrieval
     */
    public OrderDTO placeOrder(OrderDTO orderDTO, String idempotencyKey) {
        final Optional<BigDecimal> price;
        try {
            price = priceFeed.getPrice(orderDTO.symbol());
        } catch (ExhaustedRetryException e) {
            if (e.getRootCause().getMessage().contains("400 Bad Request") || e.getRootCause().getMessage().contains("404 Not Found")) {
                throw new BadRequestException(Message.PRICE_NOT_FOUND_FOR_SYMBOL.with(orderDTO.symbol()), ErrorCode.BAD_REQUEST, e);
            } else {
                throw e;
            }
        }
        final Order order = populateOrder(orderDTO, price, idempotencyKey);
        if (price.isEmpty()) {
            order.setStatus(OrderStatus.FAILED);
            return OrderMapper.INSTANCE.orderToOrderDto(orderRepo.save(order), null);
        }
        final Execution execution = populateExecution(order, price.get());
        order.setExecution(execution);
        var executionDto = ExecutionMapper.INSTANCE.mapExecutionToExecutionDto(execution, order.getOrderInternalId());

        return OrderMapper.INSTANCE.orderToOrderDto(orderRepo.save(order), executionDto);
    }

    /**
     * Retrieves an order by its internal ID.
     *
     * @param internalId the internal ID of the order
     * @return an Optional containing the OrderDTO if found, or empty if not found
     */
    public Optional<OrderDTO> getOrderByInternalId(String internalId) {
        var order = orderRepo.getOrderByOrderInternalId(internalId);
        if (order == null) {
            return Optional.empty();
        }
        var executionDto = ExecutionMapper.INSTANCE.mapExecutionToExecutionDto(order.getExecution(), order.getOrderInternalId());
        return Optional.of(OrderMapper.INSTANCE.orderToOrderDto(order, executionDto));
    }

    /**
     * Retrieves all orders associated with a specific account ID.
     *
     * @param accountId the account ID for which orders are retrieved
     * @return a list of OrderDTOs representing the orders for the account
     */
    public List<OrderDTO> getOrderByAccountId(String accountId) {
        return orderRepo.getOrderByAccountId(accountId)
                .stream()
                .map(x -> OrderMapper.INSTANCE.orderToOrderDto(x,
                        ExecutionMapper.INSTANCE.mapExecutionToExecutionDto(x.getExecution(),
                                x.getExecution().getInternalId())))
                .toList();
    }

    /**
     * Populates an Order entity based on the provided OrderDTO and price.
     * Sets the order status based on the availability of the price.
     *
     * @param orderDTO the data transfer object containing order details
     * @param price the price associated with the order, if available
     * @return a populated Order entity
     */
    private Order populateOrder(OrderDTO orderDTO, Optional<BigDecimal> price, String idempotencyKey) {
        final Order order = OrderMapper.INSTANCE.orderDtoToOrder(orderDTO, idempotencyKey);
        order.setStatus(price.isEmpty() ? OrderStatus.FAILED : OrderStatus.PROCESSED);
        return order;
    }
}