package dao;

import org.springframework.data.repository.CrudRepository;

import entity.Delivery;

public interface DeliveryRepository extends CrudRepository<Delivery, Long> {
    
}