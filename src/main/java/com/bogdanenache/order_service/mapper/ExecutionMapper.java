package com.bogdanenache.order_service.mapper;

import com.bogdanenache.order_service.dao.entity.Execution;
import com.bogdanenache.order_service.dto.ExecutionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExecutionMapper {

    ExecutionMapper INSTANCE = Mappers.getMapper(ExecutionMapper.class);

    @Mapping(target = "orderId", source = "orderId",  qualifiedByName= "orderId")
    @Mapping(target = "id", source = "execution",  qualifiedByName= "internalId")
    ExecutionDTO mapExecutionToExecutionDto(Execution execution, String orderId);

    Execution mapExecutionDtoToExecution(ExecutionDTO executionDTO);

    @Named("orderId")
    static String orderId(String orderId) {
        return orderId;
    }

    @Named("internalId")
    static String orderId(Execution execution) {
        return execution.getInternalId();
    }
}