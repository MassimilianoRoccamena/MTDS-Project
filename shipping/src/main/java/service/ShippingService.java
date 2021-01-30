package service;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.requestreply.RequestReplyFuture;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

import dao.*;
import entity.*;

@Service
public class ShippingService {

    @Autowired
    DeliveryRepository deliveryRepository;

    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    @KafkaListener(topics = "orders:shipping:deliverOrder")
    public void deliverOrder(String orderId) throws InterruptedException, ExecutionException {
        Long parsedId = Long.parseLong(orderId);
        Long deliveryManId = getAvailableDeliveryMan();
        String address = getCustomerAddress(orderId);
        Delivery delivery = new Delivery(parsedId, deliveryManId, address);
        deliveryRepository.save(delivery);
    }

    public Boolean isValidDeliveryMan(String deliveryManId) throws InterruptedException, ExecutionException {
        ProducerRecord<String, String> record = new ProducerRecord<>("shipping:users:isValidDeliveryMan", deliveryManId, deliveryManId);
        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record);
        ConsumerRecord<String, String> response = future.get();
        return Boolean.parseBoolean(response.value());
    }

    public String getCustomerAddress(String userId) throws InterruptedException, ExecutionException {
        ProducerRecord<String, String> record = new ProducerRecord<>("shipping:users:getCustomerAddress", userId, userId);
        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record);
        ConsumerRecord<String, String> response = future.get();
        return response.value();
    }

    public Long getAvailableDeliveryMan() throws InterruptedException, ExecutionException {
        ProducerRecord<String, String> record = new ProducerRecord<>("shipping:users:getAvailableDeliveryMan", "", "");
        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record);
        ConsumerRecord<String, String> response = future.get();
        return Long.parseLong(response.value());
    }
}