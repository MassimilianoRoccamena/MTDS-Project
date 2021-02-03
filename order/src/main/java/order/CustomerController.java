package order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    KafkaService kafkaService;
    
    @PostMapping("{id}/order")
	public Long submitOrder(@PathVariable Long id, @RequestBody List<Order.Field> fields) {
        try {
            
            if (!customerRepository.findById(id).isPresent()) {
                throw new OrderException("Customer " + id.toString() + " not found");
            }
    
            Order order = new Order(id, fields);
            orderRepository.save(order);
            kafkaService.notifyNewOrder(order);
            return order.getId();

        } catch (OrderException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
	}
}