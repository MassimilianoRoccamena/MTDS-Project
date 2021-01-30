package users.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import users.dao.*;
import users.entity.*;

@Configuration
class LoadDatabase {

  @Autowired
  UserRepository userRepository;

  @Bean
  CommandLineRunner initDatabase() {

    return args -> {
      userRepository.save(User.newCustomer("roccamena", "address1"));
      userRepository.save(User.newCustomer("romano", "address2"));
      userRepository.save(User.newDeliveryMan("john"));
      userRepository.save(User.newDeliveryMan("jack"));
    };
  }
}