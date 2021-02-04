package user;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import org.springframework.kafka.core.KafkaTemplate;

@Service
@Log4j2
public class KafkaService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    // @Autowired
    // KafkaTemplate<String, String> transactionalKafkaTemplate;

    public void notifyNewCustomer(Customer customer) {
        // transactionalKafkaTemplate.executeInTransaction(t -> {
        // t.send("NewCustomerName", user.getName());
        // t.send("NewCustomerAddress", user.getAddress());
        // return true;
        // });

        try {
            kafkaTemplate.send("NewCustomer", customer.getId().toString() + " " + customer.getAddress()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
        log.info("Customer " + customer.getId().toString() + " notified");
    }

    public void notifyNewDeliveryMan(DeliveryMan deliveryMan) {
        // transactionalKafkaTemplate.executeInTransaction(t -> {
        // t.send("NewCustomerName", user.getName());
        // t.send("NewCustomerAddress", user.getAddress());
        // return true;
        // });

        try {
            kafkaTemplate.send("NewDeliveryMan", deliveryMan.getId().toString()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
        log.info("Delivery man  " + deliveryMan.getId().toString() + " notified");
    }
}