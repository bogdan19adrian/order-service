package com.bogdanenache.order_service.dao.repository;

import com.bogdanenache.order_service.dao.entity.Order;
import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {


    Order getOrderByOrderInternalId(String internalId);

    Collection<Order> getOrderByAccountId(String accountId);
}
