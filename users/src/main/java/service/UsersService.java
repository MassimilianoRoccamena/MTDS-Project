package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class UsersService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "isCustomerRegistered", groupId = "groupA")
    public void isCustomerRegistered(String message) {
        kafkaTemplate.send("isCustomerRegistered_return", "1");
    }

    @KafkaListener(topics = "getCustomerAddress", groupId = "groupA")
    public void getCustomerAddress(String message) {
        kafkaTemplate.send("getCustomerAddress_return", "1");
    }
}