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
    OrderRepository orderRepository;

    @Autowired
    OrderService orderService;

    @PostMapping("{id}/order")
    public Long createOrder(@PathVariable Long id, @RequestBody List<Order.Field> fields) {
        try {
            return orderService.newOrder(id, fields);
        } catch (UserException | ProductException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    @GetMapping("{id}/orders")
    public Iterable<Order> getOrders(@PathVariable Long id) {
        return orderRepository.findAllByCustomerId(id);
    }
}