package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.BaseTest;
import com.bogdanenache.order_service.dao.entity.Order;
import com.bogdanenache.order_service.dao.entity.Order.OrderStatus;
import com.bogdanenache.order_service.dao.repository.OrderRepository;
import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.exception.UnexpectedException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderServiceTest extends BaseTest {

    private PriceFeedService priceFeed;
    private OrderRepository orderRepo;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        priceFeed = mock(PriceFeedService.class);
        orderRepo = mock(OrderRepository.class);
        orderService = new OrderService(priceFeed, orderRepo);
    }

    @Test
    void placeOrder_shouldProcessOrder_whenPriceAvailable() {
        OrderDTO orderDTO = createOrder(10, "AAPL");
        when(priceFeed.getPrice("symbol")).thenReturn(Optional.of(BigDecimal.ONE));
        Order order = new Order();
        order.setStatus(OrderStatus.PROCESSED);
        when(orderRepo.save(any())).thenReturn(order);

        OrderDTO result = orderService.placeOrder(orderDTO);

        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSED.name(), result.status());
    }

    @Test
    void placeOrder_shouldFailOrder_whenPriceNotAvailable() {
        OrderDTO orderDTO = createOrder(10, "AAPL");
        when(priceFeed.getPrice("symbol")).thenReturn(Optional.empty());
        Order order = new Order();
        order.setStatus(OrderStatus.FAILED);
        when(orderRepo.save(any())).thenReturn(order);

        OrderDTO result = orderService.placeOrder(orderDTO);

        assertNotNull(result);
        assertEquals(OrderStatus.FAILED.name(), result.status());
    }

    @Test
    void placeOrder_shouldThrow_whenPriceFeedThrows() {
        OrderDTO orderDTO = createOrder(10, "AAPL");
        when(priceFeed.getPrice("AAPL")).thenThrow(new UnexpectedException("fail"));

        assertThrows(UnexpectedException.class, () -> orderService.placeOrder(orderDTO));
    }

    @Test
    void getOrderByInternalId_shouldReturnOrder_whenFound() {
        Order order = new Order();
        order.setOrderInternalId("internalId");
        when(orderRepo.getOrderByOrderInternalId("internalId")).thenReturn(order);

        Optional<OrderDTO> result = orderService.getOrderByInternalId("internalId");

        assertTrue(result.isPresent());
    }

    @Test
    void getOrderByInternalId_shouldReturnEmpty_whenNotFound() {
        when(orderRepo.getOrderByOrderInternalId("internalId")).thenReturn(null);

        Optional<OrderDTO> result = orderService.getOrderByInternalId("internalId");

        assertTrue(result.isEmpty());
    }

    @Test
    void getOrderByAccountId_shouldReturnList() {
        Order order = new Order();
        order.setExecution(Mockito.mock(com.bogdanenache.order_service.dao.entity.Execution.class));
        when(orderRepo.getOrderByAccountId("accountId")).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrderByAccountId("accountId");

        assertEquals(1, result.size());
    }

}
