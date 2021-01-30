package controller;

import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

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

    @PostMapping("/{deliveryManId}/notify/{deliveryId}")
    public void notifyDelivery(@PathVariable String deliveryManId, @PathVariable String deliveryId)  throws InterruptedException, ExecutionException, Exception {
        Boolean validation = shippingService.isValidDeliveryMan(deliveryManId);
        if (!validation) {
            throw new Exception("Invalid delivery man");
        }

        Long parsedId = Long.parseLong(deliveryManId);
        Delivery delivery = deliveryRepository.findById(parsedId).get();
        deliver.setDelivered(Boolean.FALSE);
        deliveryRepository.save(delivery);
    }
    
}