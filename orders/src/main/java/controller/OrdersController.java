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

        Boolean validCustomer = ordersService.isCustomerRegistered(customerId.toString());
        if !(validCustomer) {
            throw new ExecutionException("Invalid customer")
        }

        String  customerAddress = ordersService.getCustomerAddress(customerId.toString());
        
        for (OrderField field : order.getFields()) {
            if !(ProductRepository.existsById(field.getProduct())) {
                throw new ExecutionException("Invalid product")
            }
        }

        ordersService.deliverOrder(order);
        orderRepository.save(order);
	}
}