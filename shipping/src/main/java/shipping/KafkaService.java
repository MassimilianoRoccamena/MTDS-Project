package shipping;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@Log4j2
public class KafkaService {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "NewDeliveryMan")
    public void onNewDeliveryMan(String message) {
        DeliveryMan deliveryMan = new DeliveryMan(Long.parseLong(message));
        log.info("Received delivery man " + deliveryMan.getId().toString());
        deliveryManRepository.save(deliveryMan);
    }

    @KafkaListener(topics = "NewOrder")
    public void onNewOrder(String message) {
        String[] splittedMessage = message.split(" ");
        Long orderId = Long.parseLong(splittedMessage[0]);
        String customerAddress = splittedMessage[1];
        DeliveryMan deliveryMan = deliveryManRepository.getRandom().get(0);
        log.info("Received order " + orderId.toString() + ", assigned to " + deliveryMan.getId().toString());
        Order order = new Order(orderId, deliveryMan.getId(), customerAddress);
        orderRepository.save(order);
    }

    public void notifyOrderDelivered(Order order) {
        try {
            kafkaTemplate.send("OrderDelivered", order.getId().toString()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
        log.info("Delivery of order " + order.getId().toString() + " notified");
    }
}