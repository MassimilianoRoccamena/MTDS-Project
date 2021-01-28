package dao;

import org.springframework.data.repository.CrudRepository;

import entity.Customer;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    
}