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
    OrderRepository orderRepository;

    @Autowired
    KafkaService kafkaService;

    @PostMapping("/{deliveryManId}/deliver/{orderId}")
    public void notifyDelivery(@PathVariable Long deliveryManId, @PathVariable Long orderId) {
        if (!deliveryManRepository.findById(deliveryManId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery man " + deliveryManId.toString() + " not found");
        }

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (!optionalOrder.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + orderId.toString() + " not found");
        }
        if (!optionalOrder.get().getDeliveryManId().equals(deliveryManId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order " + orderId.toString() + " is not assigned to " + deliveryManId.toString());
        }
        if (optionalOrder.get().getDelivered()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order " + orderId.toString() + " has been already delivered");
        }

        Order order = optionalOrder.get();
        order.setDelivered(Boolean.TRUE);
        orderRepository.save(order);
        kafkaService.notifyOrderDelivered(order);
    }

    @GetMapping("/{id}/myOrders")
    public Iterable<Order> getMyOrders(@PathVariable Long id) {
        return orderRepository.findAllByDeliveryManId(id);
    }
}