package order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class KafkaService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "NewCustomer")
    public void onNewCustomer(String message) {
        String[] splittedMessage = message.split(" ");
        Long customerId = Long.parseLong(splittedMessage[0]);
        String customerAddress = splittedMessage[1];
        Customer customer = new Customer(customerId, customerAddress);
        customerRepository.save(customer);
    }
    
    public void notifyNewOrder(Order order) {
        Customer customer = customerRepository.findById(order.getCustomerId()).get();
        kafkaTemplate.send("NewOrder", order.getId().toString() + " " + customer.getAddress());
    }
}