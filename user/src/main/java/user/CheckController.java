package user;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/check")
public class CheckController {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @GetMapping("/customer/all")
    public Iterable<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    @GetMapping("/deliveryMan/all")
    public Iterable<DeliveryMan> getAllDeliveryMen() {
        return deliveryManRepository.findAll();
    }
}