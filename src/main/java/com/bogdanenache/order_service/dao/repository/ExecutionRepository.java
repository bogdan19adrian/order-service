package com.bogdanenache.order_service.dao.repository;

import com.bogdanenache.order_service.dao.entity.Execution;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


/**
 * This interface is used to access the Execution entity in the database.
 * It extends the CrudRepository interface, which provides methods for basic CRUD operations.
 * It was added knowing that at this stage it will not be used, but  it is in place for future use.
 */
@Repository
public interface ExecutionRepository extends CrudRepository<Execution, Long> {

}
