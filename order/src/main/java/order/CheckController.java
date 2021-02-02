package order;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/check")
public class CheckController {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @GetMapping("/customer/all")
    public Iterable<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    @GetMapping("/product/all")
    public Iterable<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/order/all")
    public Iterable<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}