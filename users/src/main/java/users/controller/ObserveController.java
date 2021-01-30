package users.controller;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import users.dao.*;
import users.entity.*;

@RestController
@RequestMapping("/get")
public class ObserveController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/all")
    public Iterable<User> getAll() {
        return userRepository.findAll();
    }
    
}