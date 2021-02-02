package order;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
	public Long submitOrder(@PathVariable Long id, @RequestBody List<Order.Field> fields) throws OrderException {
        Optional<Customer> customer = customerRepository.findById(id);
        if (!customer.isPresent()) {
            throw new OrderException("Customer " + id.toString() + " not found");
        }

        Order order = new Order(id, fields);
        orderRepository.save(order);
        kafkaService.notifyNewOrder(order);
        return order.getId();
	}
}