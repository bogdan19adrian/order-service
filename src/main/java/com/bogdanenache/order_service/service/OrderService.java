package com.bogdanenache.order_service.service;


import com.bogdanenache.order_service.dao.entity.Execution;
import com.bogdanenache.order_service.dao.entity.Order;
import com.bogdanenache.order_service.dao.entity.Order.OrderStatus;
import com.bogdanenache.order_service.dao.repository.ExecutionRepository;
import com.bogdanenache.order_service.dao.repository.OrderRepository;
import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.mapper.ExecutionMapper;
import com.bogdanenache.order_service.mapper.OrderMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PriceFeedService priceFeed;

    private final OrderRepository orderRepo;
    private final ExecutionRepository executionRepo;

    public OrderDTO placeOrder(OrderDTO orderDTO) {

        final BigDecimal price = priceFeed.getPrice(orderDTO.symbol());
        final Order order = populateOrder(orderDTO);
        final Execution execution = populateExecution(order, price);

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

    private static Execution populateExecution(Order order, BigDecimal price) {
        return Execution.builder()
                .order(order)
                .price(price)
                .internalId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .build();
    }

    private Order populateOrder(OrderDTO orderDTO) {
        final Order order = OrderMapper.INSTANCE.orderDtoToOrder(orderDTO);
        order.setStatus(OrderStatus.PROCESSED);
        return order;
    }
}
