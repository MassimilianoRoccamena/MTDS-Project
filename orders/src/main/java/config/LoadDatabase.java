package config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import entity.Product;
import dao.ProductRepository;

@Configuration
class LoadDatabase {

  @Bean
  CommandLineRunner initDatabase(ProductRepository productRepository) {

    return args -> {
      productRepository.save(new Product("hamburger", "firm1"));
      productRepository.save(new Product("chips", "firm2"));
      productRepository.save(new Product("chicken", "firm1"));
      productRepository.save(new Product("salad", "firm2"));
    };
  }
}