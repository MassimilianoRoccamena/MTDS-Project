package shipping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import org.springframework.kafka.annotation.KafkaListener;

@Service
@Log4j2
public class UserService {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @KafkaListener(topics = "NewDeliveryMan")
    public void onNewDeliveryMan(String message) {

        // If (delivery man exists):  exception
        // Else:                      save customer
        Long userId = Long.parseLong(message);
        log.info("Received delivery man " + userId.toString());
        if (deliveryManRepository.findById(userId).isPresent()) {
            log.error("Customer " + userId.toString() + " already exists");
        }

        DeliveryMan deliveryMan = new DeliveryMan(userId);
        deliveryManRepository.save(deliveryMan);
    }
}
