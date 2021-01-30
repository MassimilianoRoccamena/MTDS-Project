package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.requestreply.RequestReplyFuture;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

import entity.Order;

@Service
public class OrdersService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    public Boolean isCustomerRegistered(String userId) throws InterruptedException, ExecutionException {
        ProducerRecord<String, String> record = new ProducerRecord<>("orders:users:isCustomerRegistered", userId, userId);
        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record);
        ConsumerRecord<String, String> response = future.get();
        return Boolean.parseBoolean(response.value());
    }

    public void deliverOrder(Order order) {
        kafkaTemplate.send(order.Id.toString());
    }
}