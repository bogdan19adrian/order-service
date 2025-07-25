package com.bogdanenache.order_service.dao.repository;

import com.bogdanenache.order_service.dao.entity.Execution;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
public interface ExecutionRepository extends CrudRepository<Execution, Long> {

}
