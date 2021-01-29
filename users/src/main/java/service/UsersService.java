package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;

@Service
public class UsersService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "users:orders:isCustomerRegistered")
    public void isCustomerRegistered(String message) {
        String response = "1";
        kafkaTemplate.send("users:orders:isCustomerRegistered_return", "1");
    }

    @KafkaListener(topics = "users:orders:getCustomerAddress")
    public void getCustomerAddress(String message) {
        String response = "1";
        kafkaTemplate.send("users:orders:getCustomerAddress_return", "1");
    }
}