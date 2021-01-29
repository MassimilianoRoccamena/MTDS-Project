package dao;

import org.springframework.data.repository.CrudRepository;

import entity.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {
    
}