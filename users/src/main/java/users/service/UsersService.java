package users.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import users.dao.*;
import users.entity.*;

@Service
public class UsersService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    @KafkaListener(topics = "orders:users:isValidCustomer")
    @SendTo("users:orders:isValidCustomer")
    public Boolean isValidCustomer(String customerId) {
        Long parsedId = Long.parseLong(customerId);
        User customer = userRepository.findById(parsedId).get();
        return customer.getCustomer();
    }

    @KafkaListener(topics = "shipping:users:isValidDeliveryMan")
    @SendTo("shipping:isValidDeliveryMan")
    public Boolean isValidDeliveryMan(String customerId) {
        Long parsedId = Long.parseLong(customerId);
        User deliveryMan = userRepository.findById(parsedId).get();
        return !deliveryMan.getCustomer();
    }

    @KafkaListener(topics = "orders:users:getCustomerAddress")
    @SendTo("users:orders:getCustosmerAddress")
    public String getCustomerAddress(String customerId) {
        Long parsedId = Long.parseLong(customerId);
        User customer = userRepository.findById(parsedId).get();
        return customer.getAddress();
    }

    @KafkaListener(topics = "shipping:users:getAvailableDeliveryMan")
    @SendTo("users:shipping:getAvailableDeliveryMan")
    public Long getAvailableDeliveryMan(String nothing) {
        User deliveryMan = userRepository.findById(Long.valueOf("2")).get();
        return deliveryMan.getId();
    }
}