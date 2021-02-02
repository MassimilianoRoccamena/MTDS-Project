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
    public void notifyDelivery(@PathVariable Long deliveryManId, @PathVariable Long deliveryId) throws ShippingException {
        Optional<DeliveryMan> deliveryMan = deliveryManRepository.findById(deliveryManId);
        if (!deliveryMan.isPresent()) {
            throw new ShippingException("Delivery man " + deliveryManId.toString() + " not found");
        }

        Optional<Delivery> delivery = deliveryRepository.findById(deliveryId);
        if (!delivery.isPresent()) {
            throw new ShippingException("Delivery " + deliveryId.toString() + " not found");
        }
        if (!delivery.get().getDeliveryManId().equals(deliveryManId)) {
            throw new ShippingException("Delivery " + deliveryId.toString() + " is not assigned to " + deliveryManId.toString());
        }
        delivery.get().setDelivered(Boolean.TRUE);
    }
}