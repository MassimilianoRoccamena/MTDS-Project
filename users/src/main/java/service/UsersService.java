package service;

import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class UsersService {

    @KafkaListener(topics = "isCustomerRegistered", groupId = "groupA")
    public void isCustomerRegistered(String message) {
        
    }

    @KafkaListener(topics = "getCustomerAddress", groupId = "groupA")
    public void getCustomerAddress(String message) {
        
    }
}