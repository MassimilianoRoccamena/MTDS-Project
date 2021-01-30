package controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dao.*;
import entity.*;
import service.*;

@RestController
@RequestMapping("/target")
public class OrdersController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrdersService ordersService;
    
    @PostMapping("/{customerId}/submit")
	public void submitOrder(@PathVariable String customerId, @RequestBody List<OrderField> orderFields) throws InterruptedException, ExecutionException {
        Long customerId = Long.parseLong(customerId);

        Boolean validation = ordersService.isCustomerRegistered(customerId.toString());
        if !(validation) {
            throw new ExecutionException("Invalid customer")
        }
        
        for (OrderField field : orderFields) {
            if !(ProductRepository.existsById(field.getProduct())) {
                throw new ExecutionException("Invalid product")
            }
        }

        // Transaction?
        Order order = new Order(customerId, orderFields);
        orderRepository.save(order);
        ordersService.deliverOrder(order);
	}
}