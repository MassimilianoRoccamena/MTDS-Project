package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dto.LoginForm;
import dao.CustomerRepository;
import entity.Customer;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    CustomerRepository customerRepository;
    
    @PostMapping("/register")
	public Long registerUser(@RequestBody LoginForm loginForm) {
        Customer customer = Customer.fromDTO(loginForm);
        customerRepository.save(customer);
        return customer.getId();
	}
}