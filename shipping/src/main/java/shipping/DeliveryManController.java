package shipping;

import java.util.Optional;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/deliveryMan")
public class DeliveryManController {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    DeliveryRepository deliveryRepository;

    @Autowired
    KafkaService kafkaService;

    @PostMapping("/{deliveryManId}/deliver/{deliveryId}")
    public void notifyDelivery(@PathVariable Long deliveryManId, @PathVariable Long deliveryId) {
        if (!deliveryManRepository.findById(deliveryManId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery man " + deliveryManId.toString() + " not found");
        }

        Optional<Delivery> optionalDelivery = deliveryRepository.findById(deliveryId);
        if (!optionalDelivery.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery " + deliveryId.toString() + " not found");
        }
        if (!optionalDelivery.get().getDeliveryManId().equals(deliveryManId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery " + deliveryId.toString() + " is not assigned to " + deliveryManId.toString());
        }
        if (optionalDelivery.get().getDelivered()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery " + deliveryId.toString() + " has been already delivered");
        }

        Delivery delivery = optionalDelivery.get();
        delivery.setDelivered(Boolean.TRUE);
        deliveryRepository.save(delivery);
        kafkaService.notifyOrderDelivered(delivery);
    }
}