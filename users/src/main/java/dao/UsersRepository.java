package dao;

import org.springframework.data.repository.CrudRepository;

import entity.User;

public interface UserRepository extends CrudRepository<User, Long> {
    
}