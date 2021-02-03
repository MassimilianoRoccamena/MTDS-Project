package shipping;

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
    DeliveryRepository deliveryRepository;

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
        log.info("Received order " + orderId.toString());
        String customerAddress = splittedMessage[1];
        DeliveryMan deliveryMan = deliveryManRepository.getRandom().get(0);
        Delivery delivery = new Delivery(orderId, deliveryMan.getId(), customerAddress);
        deliveryRepository.save(delivery);
    }

    public void notifyOrderDelivered(Delivery delivery) {
        kafkaTemplate.send("OrderDelivered", delivery.getOrderId().toString());
        log.info("Delivery " + delivery.getOrderId().toString() + " notified");
    }
}