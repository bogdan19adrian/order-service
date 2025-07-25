package com.bogdanenache.order_service.dao.repository;

import com.bogdanenache.order_service.dao.entity.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {


}
