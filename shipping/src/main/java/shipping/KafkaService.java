package shipping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import org.springframework.kafka.annotation.KafkaListener;

@Service
@Log4j2
public class KafkaService {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    DeliveryRepository deliveryRepository;

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
}