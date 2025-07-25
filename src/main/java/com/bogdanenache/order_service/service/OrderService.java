package com.bogdanenache.order_service.service;


import com.bogdanenache.order_service.dao.entity.Execution;
import com.bogdanenache.order_service.dao.entity.Order;
import com.bogdanenache.order_service.dao.entity.Order.OrderStatus;
import com.bogdanenache.order_service.dao.repository.ExecutionRepository;
import com.bogdanenache.order_service.dao.repository.OrderRepository;
import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.mapper.OrderMapper;
import java.math.BigDecimal;
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

        final Order order = OrderMapper.INSTANCE.orderDtoToOrder(orderDTO);
        order.setOrderInternalId(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PROCESSED);

        final Execution execution = Execution.builder()
                .order(order)
                .price(price)
                .internalId(UUID.randomUUID().toString())
                .build();

        order.setExecution(execution); // link both ways
        return OrderMapper.INSTANCE.orderToOrderDto(orderRepo.save(order), execution);

    }

    public Optional<OrderDTO> getOrderByInternalId(String internalId) {
        var order = orderRepo.getOrderByOrderInternalId(internalId);
        if (order == null) {
            return Optional.empty();
        }
        return Optional.of(OrderMapper.INSTANCE.orderToOrderDto(order, order.getExecution()));
    }

    public List<OrderDTO> getOrderByAccountId(String accountId) {
        return orderRepo.getOrderByAccountId(accountId)
                .stream()
                .map(x -> OrderMapper.INSTANCE.orderToOrderDto(x, x.getExecution()))
                .toList();
    }
}
