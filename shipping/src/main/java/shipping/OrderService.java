package shipping;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@Log4j2
public class OrderService {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "NewOrder")
    public void onNewOrder(String message) {

        // If (order exists):  exception
        // Else:               save order assigned to delivery man
        String[] splittedMessage = message.split(" ");
        Long orderId = Long.parseLong(splittedMessage[0]);
        log.info("Received order " + orderId.toString());
        if (orderRepository.findById(orderId).isPresent()) {
            log.error("Order " + orderId.toString() + " already exists", HttpStatus.BAD_REQUEST);
            return;
        }

        String customerAddress = splittedMessage[1];
        DeliveryMan deliveryMan = deliveryManRepository.getRandom().get(0);
        log.info("Order " + orderId.toString() + " assigned to delivery man " + deliveryMan.getId().toString());
        Order order = new Order(orderId, deliveryMan.getId(), customerAddress);
        orderRepository.save(order);
    }

    public void deliverOrder(Long userId, Long orderId) throws UserException, OrderException {

        // If (delivery man or order not found or invalid order or delivery man):   exception
        // Else:                                                                    update order
        if (!deliveryManRepository.findById(userId).isPresent()) {
            throw new UserException("Delivery man " + userId.toString() + " not found");
        }

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (!optionalOrder.isPresent()) {
            throw new OrderException("Order " + orderId.toString() + " not found", HttpStatus.NOT_FOUND);
        }
        if (!optionalOrder.get().getDeliveryManId().equals(userId)) {
            throw new OrderException("Order " + orderId.toString() + " is not assigned to " + userId.toString(), HttpStatus.BAD_REQUEST);
        }
        if (optionalOrder.get().getDelivered()) {
            throw new OrderException("Order " + orderId.toString() + " has been already delivered", HttpStatus.BAD_REQUEST);
        }

        // Notify delivered order id
        Order order = optionalOrder.get();
        order.setDelivered(Boolean.TRUE);
        orderRepository.save(order);
        kafkaTemplate.send("OrderDelivered", order.getId().toString());
        log.info("Delivery of order " + order.getId().toString() + " notified");
    }
}