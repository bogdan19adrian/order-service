package com.bogdanenache.order_service.service;


import com.bogdanenache.order_service.dao.entity.Execution;
import com.bogdanenache.order_service.dao.entity.Order;
import com.bogdanenache.order_service.dao.repository.ExecutionRepository;
import com.bogdanenache.order_service.dao.repository.OrderRepository;
import com.bogdanenache.order_service.dto.OrderDTO;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PriceFeedService priceFeed;
    private final ModelMapper modelMapper;

    private final OrderRepository orderRepo;
    private final ExecutionRepository executionRepo;

    public OrderDTO placeOrder(OrderDTO orderDTO) {

        final BigDecimal price = priceFeed.getPrice(orderDTO.symbol());

        final Order order = modelMapper.map(orderDTO, Order.class);
        order.setOrderInternalId(UUID.randomUUID().toString());

        final Execution execution = Execution.builder()
                .orderId(order.getId())
                .price(price)
                .internalId(UUID.randomUUID().toString())
                .build();

        order.setExecution(execution); // link both ways

        return modelMapper.map(orderRepo.save(order), OrderDTO.class);

    }

}
