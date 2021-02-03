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

    @PostMapping("/{deliveryManId}/delivered/{deliveryId}")
    public void notifyDelivery(@PathVariable Long deliveryManId, @PathVariable Long deliveryId) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        try {
            
            if (!deliveryManRepository.findById(deliveryManId).isPresent()) {
                throw new ShippingException("Delivery man " + deliveryManId.toString() + " not found");
            }

            Optional<Delivery> delivery = deliveryRepository.findById(deliveryId);
            if (!delivery.isPresent()) {
                throw new ShippingException("Delivery " + deliveryId.toString() + " not found");
            }
            if (!delivery.get().getDeliveryManId().equals(deliveryManId)) {
                httpStatus = HttpStatus.BAD_REQUEST;
                throw new ShippingException("Delivery " + deliveryId.toString() + " is not assigned to " + deliveryManId.toString());
            }
            delivery.get().setDelivered(Boolean.TRUE);

        } catch (ShippingException ex) {
            throw new ResponseStatusException(httpStatus, ex.getMessage());
        }
    }
}