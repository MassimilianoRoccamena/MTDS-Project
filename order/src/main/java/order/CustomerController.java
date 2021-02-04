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
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    KafkaService kafkaService;
    
    @PostMapping("{id}/order")
	public Long submitOrder(@PathVariable Long id, @RequestBody List<Order.Field> fields) {
        if (!customerRepository.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer " + id.toString() + " not found");
        }

        for (Order.Field field : fields) {
            if (!productRepository.findByName(field.getName()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product " + field.getName() + " not found");
            }
        }

        Order order = new Order(id, fields);
        orderRepository.save(order);
        kafkaService.notifyNewOrder(order);
        return order.getId();
    }
    
    @GetMapping("{id}/myOrders")
    public Iterable<Order> getMyOrders(@PathVariable Long id) {
        return orderRepository.findAllByCustomerId(id);
    }
}