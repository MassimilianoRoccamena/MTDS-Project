package dao;

import org.springframework.data.repository.CrudRepository;

import entity.Order;

public interface OrderRepository extends CrudRepository<Order, Long> {
    
}