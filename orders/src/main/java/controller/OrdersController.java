package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dao.*;
import entity.*;
import service.*;

@RestController
@RequestMapping("/orders")
public class UsersController {

    @Autowired
    OrderRepository customerRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrdersService ordersService;
    
    @PostMapping("/create")
	public void createOrder(@RequestBody Order order) {
        Long customerId = order.customerId();
        Boolean validCustomer = ordersService.isCustomerRegistered(customerId);
        String  customerAddress = ordersService.getCustomerAddress(customerId);
        ordersService.save(order);
	}
}