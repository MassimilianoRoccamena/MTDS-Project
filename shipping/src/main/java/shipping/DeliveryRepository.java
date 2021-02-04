package shipping;

import org.springframework.data.repository.CrudRepository;

public interface DeliveryRepository extends CrudRepository<Delivery, Long> {
    
    Iterable<Delivery> findAllByDeliveryManId(Long deliveryManId);
}