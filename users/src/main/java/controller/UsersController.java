package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dto.*;
import dao.CustomerRepository;
import entity.Customer;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    CustomerRepository customerRepository;

	//@GetMapping(value = "/customer/{id}")
	//public @ResponseBody Customer getCustomerById(@PathVariable("id") Long id)
	//@PutMapping("/customer/{id}/address")
	//public void putCustomerAddress(@RequestBody String address)
    
    @PostMapping("/register")
	public Long registerUser(@RequestBody LoginForm loginForm) {
        Customer customer = Customer.fromDTO(loginForm);
        customerRepository.save(customer);
        return customer.getId();
	}
}