package com.bogdanenache.order_service.mapper;

import com.bogdanenache.order_service.dao.entity.Order;
import com.bogdanenache.order_service.dto.ExecutionDTO;
import com.bogdanenache.order_service.dto.OrderDTO;
import java.time.Instant;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "execution", source = "executionDTO")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "id", source = "order.orderInternalId")
    OrderDTO orderToOrderDto(Order order, ExecutionDTO executionDTO);

    @Mapping(target = "createdAt", source = "orderDTO", qualifiedByName = "createdAt")
    @Mapping(target = "orderInternalId", source = "orderDTO", qualifiedByName = "mapToOrderInternalId")
    Order orderDtoToOrder(OrderDTO orderDTO);

    @Named("mapToOrderInternalId")
    static String mapToOrderInternalId(OrderDTO orderDTO) {
        return UUID.randomUUID().toString();
    }

    @Named("createdAt")
    static Instant createdAt(OrderDTO orderDTO) {
        return Instant.now();
    }
}
