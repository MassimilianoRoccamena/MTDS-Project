package user;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface DeliveryManRepository extends CrudRepository<DeliveryMan, Long> {
    
    Optional<DeliveryMan> findByName(String name);
}