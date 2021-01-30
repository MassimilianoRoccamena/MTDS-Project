package controller;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dao.*;
import entity.*;

@RestController
@RequestMapping("/target")
public class OrdersController {

    @Autowired
    DeliveryRepository deliveryRepository;

    @Autowired
    ShippingService ordersService;

    @GetMapping("/{deliveryManId}/notify")
    public Boolean notifyDelivery(@PathVariable String deliveryManId)  throws InterruptedException, ExecutionException {
        Boolean parsedId = Boolean.parseBoolean()deliveryManId;

        Boolean validation = ordersService.isValidDeliveryMan(parsedId);
        if !(validation) {
            throw new ExecutionException("Invalid delivery man")
        }

        String address = ordersService.getCustomerAddress();

        Delivery delivery = new Delivery(parsedId, address);
        deliveryRepository.save(delivery);
        return Boolean.TRUE;
    }
    
}