package user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    KafkaService kafkaService;
    
    @PostMapping("/register/{name}/{address}")
	public Long registerCustomer(@PathVariable String name, @PathVariable String address) {
        Customer customer = new Customer(name, address);
        customerRepository.save(customer);
        kafkaService.notifyNewCustomer(customer);
        return customer.getId();
	}
}