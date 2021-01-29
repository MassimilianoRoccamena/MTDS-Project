package service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;

import dao.CustomerRepository;
import entity.Customer;

@Service
public class UsersService {

    @Autowired
    CustomerRepository customerRepository;

    @KafkaListener(topics = "orders:users:isCustomerRegistered")
    @SendTo("users:orders:isCustomerRegistered")
    public Boolean isCustomerRegistered(String customerId) {
        Long parsedId = Long.parseLong(customerId);
        return customerRepository.existsById(parsedId);
    }

    @KafkaListener(topics = "orders:users:getCustomerAddress")
    @SendTo("users:orders:getCustomerAddress")
    public String getCustomerAddress(String customerId) {
        Long parsedId = Long.parseLong(customerId);
        Optional<Customer> customer = customerRepository.findById(parsedId);
        return customer.get().getAddress();
    }
}