package shipping;

import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/check")
public class CheckController {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    OrderRepository orderRepository;
    
    @GetMapping("/delivery/all")
    public Iterable<DeliveryMan> getAllDeliveryMen() {
        return deliveryManRepository.findAll();
    }

    @GetMapping("/order/all")
    public Iterable<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}