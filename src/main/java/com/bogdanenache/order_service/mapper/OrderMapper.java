package com.bogdanenache.order_service.mapper;

import com.bogdanenache.order_service.dao.entity.Execution;
import com.bogdanenache.order_service.dao.entity.Order;
import com.bogdanenache.order_service.dto.ExecutionDTO;
import com.bogdanenache.order_service.dto.OrderDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "execution", source = "execution",  qualifiedByName= "executionToExecutionDto")
    OrderDTO orderToOrderDto(Order order, Execution execution);

    Order orderDtoToOrder(OrderDTO orderDTO);

    @Named("executionToExecutionDto")
    static ExecutionDTO mapExecutionToExecutionDto(Execution execution) {
        return ExecutionMapper.INSTANCE.orderToOrderDto(execution);
    }
}