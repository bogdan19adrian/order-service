package com.bogdanenache.order_service.dao.repository;

import com.bogdanenache.order_service.dao.entity.Order;
import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface OrderRepository provides methods to access and manipulate Order entities in the database.
 * It extends CrudRepository to inherit basic CRUD operations.
 * Custom query methods are defined to retrieve orders by their internal ID and account ID.
 */

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {


    Order getOrderByOrderInternalId(String internalId);

    Collection<Order> getOrderByAccountId(String accountId);
}
