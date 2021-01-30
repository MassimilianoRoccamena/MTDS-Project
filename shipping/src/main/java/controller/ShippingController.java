package controller;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipping")
public class OrdersController {

    @GetMapping("/{id}/deliver")
    public List<String> toDo(@PathVariable String deliveryManId) {

    }
    
}