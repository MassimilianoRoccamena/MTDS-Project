package shipping;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DeliveryManRepository extends CrudRepository<DeliveryMan, Long> {
    
    @Query("SELECT * FROM DeliveryMan ORDER BY RAND() LIMIT 1")
    public List<DeliveryMan> getRandom();
}