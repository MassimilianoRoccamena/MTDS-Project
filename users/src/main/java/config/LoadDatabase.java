package config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dao.*;
import entity.*;

@Configuration
class LoadDatabase {

  @Autowired
  UserRepository userRepository;

  @Bean
  CommandLineRunner initDatabase() {

    return args -> {
      userRepository.save(new User("roccamena", "address1"));
      userRepository.save(new User("romano", "address2"));
    };
  }
}