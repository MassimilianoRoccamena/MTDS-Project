package controller;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dao.*;
import entity.*;
import service.*;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrdersService ordersService;
    
    @PostMapping("/create")
	public void createOrder(@RequestBody Order order) throws InterruptedException, ExecutionException {
        Long customerId = order.getCustomerId();

        Boolean validation = ordersService.isCustomerRegistered(customerId.toString());
        if !(validation) {
            throw new ExecutionException("Invalid customer")
        }
        
        for (OrderField field : order.getFields()) {
            if !(ProductRepository.existsById(field.getProduct())) {
                throw new ExecutionException("Invalid product")
            }
        }

        // Transaction?
        orderRepository.save(order);
        ordersService.deliverOrder(order);
	}
}