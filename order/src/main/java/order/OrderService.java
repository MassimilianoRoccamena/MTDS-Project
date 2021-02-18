package order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@Log4j2
public class OrderService {

    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public Long newOrder(Long userId, List<Order.Field> fields) throws UserException, ProductException {

        // If (customer or product not found):  exception
        // Else:                                save order
        if (!customerRepository.findById(userId).isPresent()) {
            throw new UserException("Customer " + userId.toString() + " not found");
        }
        for (Order.Field field : fields) {
            if (!productRepository.findByName(field.getName()).isPresent()) {
                throw new ProductException("Product " + field.getName() + " not found");
            }
        }
        Order order = new Order(userId, fields);
        orderRepository.save(order);

        // Notify order id and customer address
        Customer customer = customerRepository.findById(order.getCustomerId()).get();
        kafkaTemplate.send("NewOrder", order.getId().toString() + " " + customer.getAddress());
        log.info("Order " + order.getId().toString() + " notified");

        return order.getId();
    }

    @KafkaListener(topics = "OrderDelivered")
    public void onOrderDelivered(String message) throws OrderException {

        // If (order not found):  exception
        // Else:                  update order
        Long orderId = Long.parseLong(message);
        log.info("Received delivery of order " + orderId.toString());
        if (!orderRepository.findById(orderId).isPresent()) {
            throw new OrderException("Order " + orderId.toString() + " not found");
        }

        Order order = orderRepository.findById(orderId).get();
        order.setDelivered(Boolean.TRUE);
        orderRepository.save(order);
    }
}