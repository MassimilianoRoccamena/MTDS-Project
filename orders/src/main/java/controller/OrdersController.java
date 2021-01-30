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
	public void submitOrder(@PathVariable String customerId, @RequestBody List<OrderField> orderFields) throws InterruptedException, ExecutionException, Exception {
        Boolean validation = ordersService.isCustomerRegistered(customerId);
        if (!validation) {
            throw new Exception("Invalid customer");
        }
        
        for (OrderField field : orderFields) {
            if (!productRepository.existsById(field.getProductId())) {
                throw new Exception("Invalid product");
            }
        }

        // Transaction?
        Long parsedId = Long.parseLong(customerId);
        Order order = new Order(parsedId, orderFields);
        orderRepository.save(order);
        ordersService.deliverOrder(order);
	}
}