package dao;

import org.springframework.data.repository.CrudRepository;

import entity.DeliveryMan;

public interface DeliveryManRepository extends CrudRepository<DeliveryMan, Long> {
    
}