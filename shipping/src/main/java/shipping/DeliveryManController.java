package shipping;

import java.util.Optional;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/deliveryMan")
public class DeliveryManController {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    DeliveryRepository deliveryRepository;

    @PostMapping("/{deliveryManId}/delivered/{deliveryId}")
    public void notifyDelivery(@PathVariable Long deliveryManId, @PathVariable Long deliveryId) {
        Optional<DeliveryMan> deliveryMan = deliveryManRepository.findById(deliveryManId);
        if (!deliveryMan.isPresent()) {
            // Invalid user
        }

        Optional<Delivery> delivery = deliveryRepository.findById(deliveryId);
        if (!delivery.isPresent()) {
            // Invalid delivery
        }
        if (!delivery.get().getDeliveryManId().equals(deliveryManId)) {
            // Invalid delivery
        }
        delivery.get().setDelivered(Boolean.TRUE);
    }
}