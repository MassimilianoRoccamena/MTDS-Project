package order;

import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {
    
    Iterable<Order> findAllByCustomerId(Long customerId);
}