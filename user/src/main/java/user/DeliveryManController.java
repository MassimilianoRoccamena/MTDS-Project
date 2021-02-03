package user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/deliveryMan")
public class DeliveryManController {

    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    KafkaService kafkaService;
    
    @PostMapping("/register/{name}")
	public Long registerDeliveryMan(@PathVariable String name) {
        try {
            
            if (deliveryManRepository.findByName(name).isPresent()) {
                throw new UserException("Delivery man " + name + " already exists");
            }
    
            DeliveryMan deliveryMan = new DeliveryMan(name);
            deliveryManRepository.save(deliveryMan);
            kafkaService.notifyNewDeliveryMan(deliveryMan);
            return deliveryMan.getId();

        } catch (UserException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
	}
}