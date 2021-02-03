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

        Optional<Delivery> delivery = deliveryRepository.findById(deliveryId);
        if (!delivery.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery " + deliveryId.toString() + " not found");
        }
        if (!delivery.get().getDeliveryManId().equals(deliveryManId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery " + deliveryId.toString() + " is not assigned to " + deliveryManId.toString());
        }
        if (delivery.get().getDelivered()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery " + deliveryId.toString() + " has been already delivered");
        }

        delivery.get().setDelivered(Boolean.TRUE);
        kafkaService.notifyOrderDelivered(delivery.get());
    }
}