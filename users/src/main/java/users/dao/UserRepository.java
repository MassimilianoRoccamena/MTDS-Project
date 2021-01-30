package users.dao;

import org.springframework.data.repository.CrudRepository;

import users.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {
    
}