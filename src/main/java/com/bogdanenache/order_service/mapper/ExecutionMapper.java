package com.bogdanenache.order_service.mapper;

import com.bogdanenache.order_service.dao.entity.Execution;
import com.bogdanenache.order_service.dto.ExecutionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExecutionMapper {

    ExecutionMapper INSTANCE = Mappers.getMapper(ExecutionMapper.class);

    ExecutionDTO orderToOrderDto(Execution order);

    Execution executionDtoToExecution(ExecutionDTO executionDTO);
}