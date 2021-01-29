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

    @KafkaListener(topicPartitions = @TopicPartition(topic = "isCustomerRegistered", partitions = { "0" }))
    public void isCustomerRegistered(String message) {
        String response = "1";
        kafkaTemplate.send("isCustomerRegistered", 1, response, response);
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "getCustomerAddress", partitions = { "0" }))
    public void getCustomerAddress(String message) {
        String response = "1";
        kafkaTemplate.send("getCustomerAddress", 1, response, response);
    }
}