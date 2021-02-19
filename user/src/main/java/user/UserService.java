package user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

import org.springframework.kafka.core.KafkaTemplate;

@Service
@Log4j2
public class UserService {

    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    DeliveryManRepository deliveryManRepository;

    @Autowired
    @Qualifier("newCustomerKafkaTemplate")
    KafkaTemplate<String, String> newCustomerKafkaTemplate;
    @Autowired
    @Qualifier("newDeliveryManKafkaTemplate")
    KafkaTemplate<String, String> newDeliveryManKafkaTemplate;

    @Transactional
    public Long newCustomer(String name, String address) throws UserException {

        // If (name is used):     exception
        // Else:                  save customer
        if (customerRepository.findByName(name).isPresent()) {
            throw new UserException("Customer " + name + " already exists");
        }
        Customer customer = new Customer(name, address);
        customerRepository.save(customer);

        // Notify customer name and address
        newCustomerKafkaTemplate.send("NewCustomer", customer.getId().toString() + " " + customer.getAddress());
        log.info("Customer " + customer.getId().toString() + " notified");

        return customer.getId();
    }

    @Transactional
    public Long newDeliveryMan(String name) throws UserException {

        // If (name is used):     exception
        // Else:                  save delivery man
        if (deliveryManRepository.findByName(name).isPresent()) {
            throw new UserException("Delivery man " + name + " already exists");
        }
        DeliveryMan deliveryMan = new DeliveryMan(name);
        deliveryManRepository.save(deliveryMan);

        // Notify delivery man name
        newDeliveryManKafkaTemplate.send("NewDeliveryMan", deliveryMan.getId().toString());
        log.info("Delivery man  " + deliveryMan.getId().toString() + " notified");

        return deliveryMan.getId();
    }
}