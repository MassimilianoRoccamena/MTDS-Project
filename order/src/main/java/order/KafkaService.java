package order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@Log4j2
public class KafkaService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "NewCustomer")
    public void onNewCustomer(String message) {
        String[] splittedMessage = message.split(" ");
        Long customerId = Long.parseLong(splittedMessage[0]);
        log.info("Received customer " + customerId.toString());
        String customerAddress = splittedMessage[1];
        Customer customer = new Customer(customerId, customerAddress);
        customerRepository.save(customer);
    }
    
    public void notifyNewOrder(Order order) {
        Customer customer = customerRepository.findById(order.getCustomerId()).get();
        kafkaTemplate.send("NewOrder", order.getId().toString() + " " + customer.getAddress());
        log.info("Order " + order.getId().toString() + " notified");
    }
}