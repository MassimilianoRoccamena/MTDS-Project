package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dto.*;
import dao.*;
import entity.*;

@RestController
@RequestMapping("/target")
public class UsersController {

    @Autowired
    UserRepository userRepository;
    
    @PostMapping("/register")
	public Long registerUser(@RequestBody LoginForm loginForm) {
        User customer = User.newCustomer(loginForm);
        customerRepository.save(customer);
        return customer.getId();
	}
}