package config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import entity.*;
import dao.*;

@Configuration
class LoadDatabase {

  @Bean
  CommandLineRunner initDatabase(CustomerRepository customerRepository, DeliveryManRepository deliveryManRepository) {

    return args -> {
      customerRepository.save(new Customer("max", "address1"));
      customerRepository.save(new Customer("lore", "address2"));
      deliveryManRepository.save(new DeliveryMan("max"));
      deliveryManRepository.save(new DeliveryMan("johnny"));
    };
  }
}