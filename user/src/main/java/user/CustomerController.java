package user;

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
    KafkaService kafkaService;
    
    @PostMapping("/register/{name}/{address}")
	public Long registerCustomer(@PathVariable String name, @PathVariable String address) {
        try {

            if (customerRepository.findByName(name).isPresent()) {
                throw new UserException("Customer " + name + " already exists");
            }
    
            Customer customer = new Customer(name, address);
            customerRepository.save(customer);
            kafkaService.notifyNewCustomer(customer);
            return customer.getId();

        } catch (UserException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
	}
}