package service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;

import dao.*;
import entity.*;

@Service
public class UsersService {

    @Autowired
    UserRepository userRepository;

    @KafkaListener(topics = "orders:users:isCustomerRegistered")
    @SendTo("users:orders:isCustomerRegistered")
    public Boolean isCustomerRegistered(String customerId) {
        Long parsedId = Long.parseLong(customerId);
        return userRepository.existsById(parsedId);
    }

    @KafkaListener(topics = "shipping:users:isValidDeliveryMan")
    @SendTo("shipping:isValidDeliveryMan")
    public Boolean isValidDeliveryMan(String customerId) {
        Long parsedId = Long.parseLong(customerId);
        return userRepository.existsById(parsedId);
    }

    @KafkaListener(topics = "orders:users:getCustomerAddress")
    @SendTo("users:orders:getCustosmerAddress")
    public String getCustomerAddress(String customerId) {
        Long parsedId = Long.parseLong(customerId);
        Optional<Customer> customer = customerRepository.findById(parsedId);
        return userRepository.get().getAddress();
    }

    @KafkaListener(topics = "shipping:users:getAvailableDeliveryMan")
    @SendTo("users:shipping:getAvailableDeliveryMan")
    public String getAvailableDeliveryMan(String nothing) {
        Optional<DeliveryMan> deliveryMan = deliveryManRepository.findById(Long.ZERO);
        return userRepository.get().getAddress();
    }
}