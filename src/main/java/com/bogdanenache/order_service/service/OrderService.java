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

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PriceFeedService priceFeed;

    private final OrderRepository orderRepo;

    private static Execution populateExecution(Order order, BigDecimal price) {
        return Execution.builder()
                .order(order)
                .price(price)
                .internalId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .build();
    }

    /**
     * try-catch block is used to handle specific bad request error thrown that should not enter in retrigger logic.
     */
    public OrderDTO placeOrder(OrderDTO orderDTO) {
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
        final Order order = populateOrder(orderDTO, price);
        if (price.isEmpty()) {
            // if price is not available, we cannot process the order
            order.setStatus(OrderStatus.FAILED);
            return OrderMapper.INSTANCE.orderToOrderDto(orderRepo.save(order), null);
        }
        final Execution execution = populateExecution(order, price.get());
        order.setExecution(execution); // link both ways
        var executionDto = ExecutionMapper.INSTANCE.mapExecutionToExecutionDto(execution, order.getOrderInternalId());

        return OrderMapper.INSTANCE.orderToOrderDto(orderRepo.save(order), executionDto);

    }

    public Optional<OrderDTO> getOrderByInternalId(String internalId) {
        var order = orderRepo.getOrderByOrderInternalId(internalId);
        if (order == null) {
            return Optional.empty();
        }
        var executionDto = ExecutionMapper.INSTANCE.mapExecutionToExecutionDto(order.getExecution(), order.getOrderInternalId());
        return Optional.of(OrderMapper.INSTANCE.orderToOrderDto(order, executionDto));
    }

    public List<OrderDTO> getOrderByAccountId(String accountId) {
        return orderRepo.getOrderByAccountId(accountId)
                .stream()
                .map(x -> OrderMapper.INSTANCE.orderToOrderDto(x,
                        ExecutionMapper.INSTANCE.mapExecutionToExecutionDto(x.getExecution(),
                                x.getExecution().getInternalId())))
                .toList();
    }

    private Order populateOrder(OrderDTO orderDTO, Optional<BigDecimal> price) {
        final Order order = OrderMapper.INSTANCE.orderDtoToOrder(orderDTO);
        order.setStatus(price.isEmpty() ? OrderStatus.FAILED : OrderStatus.PROCESSED);
        return order;
    }
}
