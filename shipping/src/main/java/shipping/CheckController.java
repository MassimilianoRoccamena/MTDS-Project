package shipping;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/check")
public class CheckController {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    DeliveryRepository deliveryRepository;
    
    @GetMapping("/deliveryMan/all")
    public Iterable<DeliveryMan> getAllDeliveryMen() {
        return deliveryManRepository.findAll();
    }

    @GetMapping("/delivery/all")
    public Iterable<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }
}