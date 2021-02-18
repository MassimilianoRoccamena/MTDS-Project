package order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import org.springframework.kafka.annotation.KafkaListener;

@Service
@Log4j2
public class UserService {

    @Autowired
    CustomerRepository customerRepository;

    @KafkaListener(topics = "NewCustomer")
    public void onNewCustomer(String message) throws UserException {

        // If (customer exists):  exception
        // Else:                  save customer
        String[] splittedMessage = message.split(" ");

        Long userId = Long.parseLong(splittedMessage[0]);
        log.info("Received customer " + userId.toString());
        if (customerRepository.findById(userId).isPresent()) {
            throw new UserException("Customer " + userId.toString() + " already exists");
        }

        String customerAddress = splittedMessage[1];
        Customer customer = new Customer(userId, customerAddress);
        customerRepository.save(customer);
    }
}
