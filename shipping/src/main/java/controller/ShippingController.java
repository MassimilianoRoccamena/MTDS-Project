package controller;

import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dao.*;
import entity.*;
import service.*;

@RestController
@RequestMapping("/target")
public class ShippingController {

    @Autowired
    DeliveryRepository deliveryRepository;

    @Autowired
    ShippingService shippingService;

    @GetMapping("/{deliveryManId}/notify")
    public void notifyDelivery(@PathVariable String deliveryManId)  throws InterruptedException, ExecutionException, Exception {
        Boolean parsedId = Boolean.parseBoolean(deliveryManId);

        Boolean validation = shippingService.isValidDeliveryMan(deliveryManId);
        if (!validation) {
            throw new Exception("Invalid delivery man");
        }
    }
    
}