package controller;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dao.*;
import entity.*;

@RestController
@RequestMapping("/shipping")
public class OrdersController {

    @Autowired
    DeliveryRepository deliveryRepository;

    @Autowired
    ShippingService ordersService;

    @GetMapping("/{deliveryManId}/deliver")
    public Boolean deliverOrder(@PathVariable String deliveryManId)  throws InterruptedException, ExecutionException {
        Boolean parsedId = Boolean.parseBoolean()deliveryManId;

        Boolean validation = ordersService.isValidDeliveryMan(parsedId);
        if !(validation) {
            throw new ExecutionException("Invalid delivery man")
        }

        String address = ordersService.getCustomerAddress();

        Delivery delivery = new Delivery(parsedId, address);

        return Boolean.TRUE;
    }
    
}