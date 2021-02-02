package shipping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class KafkaService {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    DeliveryRepository deliveryRepository;

    @KafkaListener(topics = "NewDeliveryMan")
    public void onNewDeliveryMan(String message) {
        DeliveryMan deliveryMan = new DeliveryMan(Long.parseLong(message));
        deliveryManRepository.save(deliveryMan);
    }

    @KafkaListener(topics = "NewOrder")
    public void onNewOrder(String message) {
        String[] splittedMessage = message.split(" ");
        Long orderId = Long.parseLong(splittedMessage[0]);
        String customerAddress = splittedMessage[1];
        Delivery delivery = new Delivery(orderId, getFreeDeliveryManId(), customerAddress);
        deliveryRepository.save(delivery);
    }

    public Long getFreeDeliveryManId() {
        return deliveryManRepository.findById(Long.parseLong("1")).get().getId();
    }
}